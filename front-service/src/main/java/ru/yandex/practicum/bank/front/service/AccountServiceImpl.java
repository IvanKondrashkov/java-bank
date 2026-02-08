package ru.yandex.practicum.bank.front.service;

import java.util.Map;
import java.util.List;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.front.client.GatewayClient;
import ru.yandex.practicum.bank.front.util.BankResponseUtil;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.account.response.AccountShortInfo;
import ru.yandex.practicum.commons.dto.account.request.CreateAccountRequest;
import ru.yandex.practicum.commons.dto.account.request.CreateOrUpdateUserProfileRequest;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final GatewayClient gatewayClient;

    @Override
    @Transactional(readOnly = true)
    public UserProfileInfo findUserProfileByUsername() {
        try {
            BankResponse<UserProfileInfo> response = gatewayClient.findUserProfileByUsername();
            BankResponseUtil.failIfNotSuccess(response);
            return response.getData();
        } catch (FeignException.NotFound ex) {
            log.info("User profile not found, will create a new one.");
            return null;
        } catch (FeignException ex) {
            log.warn("Failed to load user profile, showing empty profile.", ex);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountInfo> findAllUserAccounts() {
        try {
            BankResponse<List<AccountInfo>> response = gatewayClient.findAllUserAccounts();
            BankResponseUtil.failIfNotSuccess(response);
            return response.getData();
        } catch (FeignException ex) {
            log.warn("Failed to load accounts, showing empty list.", ex);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountShortInfo findByAccountNumber(String accountNumber) {
        try {
            BankResponse<AccountShortInfo> response = gatewayClient.findByAccountNumber(accountNumber);
            BankResponseUtil.failIfNotSuccess(response);
            return response.getData();
        } catch (FeignException ex) {
            throw BankResponseUtil.toBackendApiException(ex, "Account not found.");
        }
    }

    @Override
    public void createAccount(String username, String currency) {
        var request = new CreateAccountRequest(
                username,
                currency
        );
        try {
            BankResponse<AccountInfo> response = gatewayClient.createAccount(request);
            BankResponseUtil.failIfNotSuccess(response);
        } catch (FeignException ex) {
            throw BankResponseUtil.toBackendApiException(ex, "Failed to create account.");
        }
    }

    @Override
    public void createUserProfile(CreateOrUpdateUserProfileRequest request) {
        try {
            BankResponse<UserProfileInfo> response = gatewayClient.createUserProfile(request);
            BankResponseUtil.failIfNotSuccess(response);
        } catch (FeignException ex) {
            throw BankResponseUtil.toBackendApiException(ex, "Failed to create profile.");
        }
    }

    @Override
    public void updateUserProfile(CreateOrUpdateUserProfileRequest request) {
        try {
            BankResponse<UserProfileInfo> response = gatewayClient.updateUserProfile(request);
            BankResponseUtil.failIfNotSuccess(response);
        } catch (FeignException ex) {
            throw BankResponseUtil.toBackendApiException(ex, "Failed to update profile.");
        }
    }

    @Override
    public Map<String, BigDecimal> calculateBalanceByCurrency() {
        return findAllUserAccounts().stream()
                .collect(Collectors.groupingBy(
                        AccountInfo::getCurrency,
                        Collectors.reducing(BigDecimal.ZERO, AccountInfo::getBalance, BigDecimal::add)
                ));
    }
}