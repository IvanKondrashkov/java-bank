package ru.yandex.practicum.bank.transfer.service;

import java.util.UUID;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.bank.transfer.model.TransferType;
import ru.yandex.practicum.bank.transfer.mapper.TransferMapper;
import ru.yandex.practicum.bank.transfer.client.AccountServiceClient;
import ru.yandex.practicum.commons.dto.bank.PaymentStatus;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.transfer.response.TransferInfo;
import ru.yandex.practicum.commons.dto.transfer.response.TransferShortInfo;
import ru.yandex.practicum.commons.dto.transfer.request.TransferRequest;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;
import ru.yandex.practicum.bank.transfer.repository.TransferRepository;
import ru.yandex.practicum.bank.transfer.exception.EntityNotFoundException;
import ru.yandex.practicum.bank.transfer.exception.InvalidTransferException;
import ru.yandex.practicum.bank.transfer.exception.ServiceUnavailableException;
import ru.yandex.practicum.bank.metrics.BankMetrics;
import ru.yandex.practicum.commons.security.SecurityUtils;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final AccountServiceClient accountServiceClient;
    private final NotificationService notificationService;
    private final BankMetrics bankMetrics;

    @Override
    @Transactional(readOnly = true)
    public TransferShortInfo findById(Long id) {
        Transfer transfer = transferRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Transfer not found with id: %s", id))
        );
        return transferMapper.toTransferShortInfo(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferShortInfo> findAllByAccountNumber(String accountNumber, TransferType transferType) {
        List<Transfer> transfers = transferType == TransferType.TRANSFER_IN
                ? transferRepository.findAllByPayeeAccountNumber(accountNumber)
                : transferRepository.findAllByPayerAccountNumber(accountNumber);
        return transfers.stream()
                .map(transferMapper::toTransferShortInfo)
                .toList();
    }

    @Override
    @Retry(name = "transferService", fallbackMethod = "retryFallback")
    @CircuitBreaker(name = "transferService", fallbackMethod = "processTransferFallback")
    public TransferInfo processTransfer(TransferRequest request) {
        log.info("Processing transfer: {}", request);

        Transfer transfer = Transfer.builder()
                .payerAccountNumber(request.getPayerAccountNumber())
                .payeeAccountNumber(request.getPayeeAccountNumber())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .description(request.getDescription())
                .build();

        transferRepository.save(transfer);
        try {
            executeWithdrawal(request, transfer);
            executeDeposit(request, transfer);

            transfer.setStatus(PaymentStatus.COMPLETED);
            transferRepository.save(transfer);
            return transferMapper.toTransferInfo(transfer);
        } catch (Exception e) {
            transfer.setStatus(PaymentStatus.FAILED);
            transferRepository.save(transfer);
            throw new InvalidTransferException(String.format("Transfer failed: %s", e.getMessage()));
        }
    }

    private void executeWithdrawal(TransferRequest request, Transfer transfer) {
        String operationId = UUID.randomUUID().toString();
        UpdateBalanceRequest withdrawRequest = new UpdateBalanceRequest(
                request.getAmount(),
                TransferType.TRANSFER_OUT.name(),
                operationId,
                request.getDescription()
        );

        var response = accountServiceClient.processBalance(
                request.getPayerAccountNumber(),
                withdrawRequest
        );
        if (!response.isSuccess()) {
            transfer.setStatus(PaymentStatus.FAILED);
            transferRepository.save(transfer);
            bankMetrics.recordTransferFailure(withdrawRequest.getOperationType(), SecurityUtils.getCurrentUsername());
            throw new InvalidTransferException(String.format("Transfer failed during withdrawal: %s", response.getMessage()));
        }
        notificationService.sendNotification(response.getData(), TransferType.TRANSFER_OUT, NotificationType.PUSH);
    }

    private void executeDeposit(TransferRequest request, Transfer transfer) {
        String operationId = UUID.randomUUID().toString();
        UpdateBalanceRequest depositRequest = new UpdateBalanceRequest(
                request.getAmount(),
                TransferType.TRANSFER_IN.name(),
                operationId,
                request.getDescription()
        );

        var response = accountServiceClient.processBalance(
                request.getPayeeAccountNumber(),
                depositRequest
        );
        if (!response.isSuccess()) {
            rollbackWithdrawal(request);
            transfer.setStatus(PaymentStatus.ROLLED_BACK);
            transferRepository.save(transfer);
            bankMetrics.recordTransferFailure(depositRequest.getOperationType(), SecurityUtils.getCurrentUsername());
            throw new InvalidTransferException(String.format("Transfer failed during deposit: %s", response.getMessage()));
        }
        notificationService.sendNotification(response.getData(), TransferType.TRANSFER_IN, NotificationType.PUSH);
    }

    private void rollbackWithdrawal(TransferRequest request) {
        String operationId = UUID.randomUUID().toString();
        UpdateBalanceRequest revertRequest = new UpdateBalanceRequest(
                request.getAmount(),
                TransferType.TRANSFER_IN.name(),
                operationId,
                request.getDescription()
        );

        var response = accountServiceClient.processBalance(request.getPayerAccountNumber(), revertRequest);
        if (!response.isSuccess()) {
            log.error("Failed to revert withdrawal for account {}: {}", request.getPayerAccountNumber(), response.getMessage());
            throw new InvalidTransferException(String.format("Failed to revert withdrawal: %s", response.getMessage()));
        }
    }

    public TransferInfo retryFallback(TransferRequest request, Exception e) {
        log.error("All retries exhausted", e);
        throw new InvalidTransferException("Transfer failed after retries");
    }

    public TransferInfo processTransferFallback(TransferRequest request, Exception e) {
        log.error("Transfer fallback triggered", e);
        throw new ServiceUnavailableException("Transfer service temporarily unavailable", e);
    }
}