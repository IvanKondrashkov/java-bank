package ru.yandex.practicum.bank.cash.service;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import ru.yandex.practicum.bank.cash.exception.ServiceUnavailableException;
import ru.yandex.practicum.bank.cash.model.CashOperation;
import ru.yandex.practicum.bank.cash.mapper.CashOperationMapper;
import ru.yandex.practicum.bank.cash.client.AccountServiceClient;
import ru.yandex.practicum.commons.dto.bank.PaymentStatus;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.bank.CashOperationType;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationInfo;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationShortInfo;
import ru.yandex.practicum.commons.dto.cash.request.CashOperationRequest;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;
import ru.yandex.practicum.bank.cash.repository.CashOperationRepository;
import ru.yandex.practicum.bank.cash.exception.EntityNotFoundException;
import ru.yandex.practicum.bank.cash.exception.InvalidCashOperationException;
import ru.yandex.practicum.bank.metrics.BankMetrics;
import ru.yandex.practicum.commons.security.SecurityUtils;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CashServiceImpl implements CashService {
    private final CashOperationRepository cashOperationRepository;
    private final CashOperationMapper cashOperationMapper;
    private final AccountServiceClient accountServiceClient;
    private final NotificationService notificationService;
    private final BankMetrics bankMetrics;

    @Override
    @Transactional(readOnly = true)
    public CashOperationShortInfo findById(Long id) {
        CashOperation operation = cashOperationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Cash operation not found with id: %s", id))
        );
        return cashOperationMapper.toShortInfo(operation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashOperationShortInfo> findAllByAccountNumber(String accountNumber) {
        return cashOperationRepository.findByAccountNumber(accountNumber).stream()
                .map(cashOperationMapper::toShortInfo)
                .toList();
    }

    @Override
    @Retry(name = "cashService", fallbackMethod = "retryFallback")
    @CircuitBreaker(name = "cashService", fallbackMethod = "processOperationFallback")
    public CashOperationInfo processOperation(CashOperationRequest request) {
        log.info("Processing cash operation: {}", request);

        CashOperationType type = CashOperationType.valueOf(request.getOperationType());
        CashOperation operation = CashOperation.builder()
                .accountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .type(type)
                .status(PaymentStatus.PENDING)
                .description(request.getDescription())
                .build();

        cashOperationRepository.save(operation);
        try {
            final String operationId = UUID.randomUUID().toString();
            UpdateBalanceRequest updateBalanceRequest = new UpdateBalanceRequest(
                    request.getAmount(),
                    type.name(),
                    operationId,
                    request.getDescription()
            );

            var response = accountServiceClient.processBalance(request.getAccountNumber(), updateBalanceRequest);
            if (!response.isSuccess()) {
                operation.setStatus(PaymentStatus.FAILED);
                cashOperationRepository.save(operation);
                bankMetrics.recordCashFailure(type.name(), SecurityUtils.getCurrentUsername());
                throw new InvalidCashOperationException(String.format("Cash operation failed: %s", response.getMessage()));
            } else {
                operation.setStatus(PaymentStatus.COMPLETED);
                cashOperationRepository.save(operation);
            }

            notificationService.sendNotification(response.getData(), type, NotificationType.PUSH);
            return cashOperationMapper.toInfo(operation, operationId, response.getData().getBalance());
        } catch (Exception e) {
            log.error("Cash operation failed: {}", e.getMessage());
            bankMetrics.recordCashFailure(type.name(), SecurityUtils.getCurrentUsername());
            throw new InvalidCashOperationException(String.format("Cash operation failed: %s", e.getMessage()));
        }
    }

    public CashOperationInfo retryFallback(CashOperationRequest request, Exception e) {
        log.error("All retries exhausted", e);
        throw new InvalidCashOperationException("Cash operation failed after retries");
    }

    public CashOperationInfo processOperationFallback(CashOperationRequest request, Exception e) {
        log.error("Cash operation fallback triggered", e);
        throw new ServiceUnavailableException("Cash service temporarily unavailable", e);
    }
}