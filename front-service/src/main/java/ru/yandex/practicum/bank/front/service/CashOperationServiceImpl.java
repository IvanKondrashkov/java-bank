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
import ru.yandex.practicum.commons.dto.bank.CashOperationType;
import ru.yandex.practicum.commons.dto.cash.request.CashOperationRequest;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationInfo;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CashOperationServiceImpl implements CashOperationService {
    private final GatewayClient gatewayClient;

    @Override
    public void deposit(String accountNumber, BigDecimal amount) {
        log.info("Depositing {} to account {}", amount, accountNumber);
        createCashOperation(accountNumber, amount, CashOperationType.DEPOSIT);
    }

    @Override
    public void withdraw(String accountNumber, BigDecimal amount) {
        log.info("Withdrawing {} from account {}", amount, accountNumber);
        createCashOperation(accountNumber, amount, CashOperationType.WITHDRAWAL);
    }

    private void createCashOperation(String accountNumber, BigDecimal amount, CashOperationType cashOperationType) {
        var request = new CashOperationRequest(
                accountNumber,
                amount,
                cashOperationType.name(),
                getDescription(accountNumber, amount, cashOperationType)
        );
        try {
            BankResponse<CashOperationInfo> response = gatewayClient.createCashOperation(request);
            BankResponseUtil.failIfNotSuccess(response);
        } catch (FeignException ex) {
            throw BankResponseUtil.toBackendApiException(ex, "Failed to create cash operation.");
        }
    }

    private static String getDescription(String accountNumber, BigDecimal amount, CashOperationType type) {
        return switch (type) {
            case DEPOSIT -> String.format("Depositing %s to account %s", amount, accountNumber);
            case WITHDRAWAL -> String.format("Withdrawing %s to account %s", amount, accountNumber);
        };
    }
}