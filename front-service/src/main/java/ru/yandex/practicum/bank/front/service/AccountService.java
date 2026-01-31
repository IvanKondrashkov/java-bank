package ru.yandex.practicum.bank.front.service;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.account.response.AccountShortInfo;
import ru.yandex.practicum.commons.dto.account.request.CreateOrUpdateUserProfileRequest;

public interface AccountService {
    UserProfileInfo findUserProfileByUsername();
    List<AccountInfo> findAllUserAccounts();
    AccountShortInfo findByAccountNumber(String accountNumber);
    void createAccount(String username, String currency);
    void createUserProfile(CreateOrUpdateUserProfileRequest request);
    void updateUserProfile(CreateOrUpdateUserProfileRequest request);
    Map<String, BigDecimal> calculateBalanceByCurrency();
}