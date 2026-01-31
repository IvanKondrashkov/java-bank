package ru.yandex.practicum.bank.front.service;

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.front.client.GatewayClient;
import ru.yandex.practicum.bank.front.util.BankResponseUtil;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.transfer.response.TransferInfo;
import ru.yandex.practicum.commons.dto.account.response.AccountShortInfo;
import ru.yandex.practicum.commons.dto.transfer.request.TransferRequest;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final GatewayClient gatewayClient;
    private final AccountService accountService;

    @Override
    public void transfer(String payerAccountNumber, String payeeAccountNumber, BigDecimal amount) {
        log.info("Transferring {} from {} to {}", amount, payerAccountNumber, payeeAccountNumber);

        AccountShortInfo accountPayee = accountService.findByAccountNumber(payeeAccountNumber);
        var request = new TransferRequest(
                payerAccountNumber,
                accountPayee.getAccountNumber(),
                amount,
                String.format("Transfer %s to account %s", amount, accountPayee.getAccountNumber())

        );
        try {
            BankResponse<TransferInfo> response = gatewayClient.createTransfer(request);
            BankResponseUtil.failIfNotSuccess(response);
        } catch (FeignException ex) {
            throw BankResponseUtil.toBackendApiException(ex, "Failed to create transfer.");
        }
    }
}