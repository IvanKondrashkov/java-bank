package ru.yandex.practicum.bank.account.service;

import java.util.List;
import ru.yandex.practicum.bank.account.model.*;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.response.AccountShortInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.account.request.CreateAccountRequest;
import ru.yandex.practicum.commons.dto.account.request.CreateOrUpdateUserProfileRequest;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;

public interface AccountService {
    UserProfileInfo findUserProfileByUsername(String username);
    AccountShortInfo findByAccountNumber(String accountNumber);
    List<AccountInfo> findAllByUsername(String username);
    AccountInfo save(CreateAccountRequest request);
    UserProfileInfo createUserProfile(String username, CreateOrUpdateUserProfileRequest request);
    UserProfileInfo updateUserProfile(String username, CreateOrUpdateUserProfileRequest request);
    AccountInfo processAccountActions(String username, String accountNumber, AccountActions accountActions);
    AccountInfo processBalance(String accountNumber, UpdateBalanceRequest request);
}