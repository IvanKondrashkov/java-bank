package ru.yandex.practicum.bank.front.client;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.transfer.response.TransferInfo;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.account.response.AccountShortInfo;
import ru.yandex.practicum.commons.dto.transfer.request.TransferRequest;
import ru.yandex.practicum.commons.dto.cash.request.CashOperationRequest;
import ru.yandex.practicum.commons.dto.account.request.CreateAccountRequest;
import ru.yandex.practicum.commons.dto.account.request.CreateOrUpdateUserProfileRequest;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;

@FeignClient(name = "gateway-service", url = "${gateway.url}")
public interface GatewayClient {
    @GetMapping("/api/accounts/user/profile")
    BankResponse<UserProfileInfo> findUserProfileByUsername();
    @GetMapping("/api/accounts/user/all")
    BankResponse<List<AccountInfo>> findAllUserAccounts();
    @GetMapping("/api/accounts/number/{accountNumber}")
    BankResponse<AccountShortInfo> findByAccountNumber(@PathVariable String accountNumber);
    @PostMapping("/api/accounts")
    BankResponse<AccountInfo> createAccount(@RequestBody CreateAccountRequest request);
    @PostMapping("/api/accounts/user/profile")
    BankResponse<UserProfileInfo> createUserProfile(@RequestBody CreateOrUpdateUserProfileRequest request);
    @PutMapping("/api/accounts/user/profile")
    BankResponse<UserProfileInfo> updateUserProfile(@RequestBody CreateOrUpdateUserProfileRequest request);
    @PostMapping("/api/cash/operations")
    BankResponse<CashOperationInfo> createCashOperation(@RequestBody CashOperationRequest request);
    @PostMapping("/api/transfers")
    BankResponse<TransferInfo> createTransfer(@RequestBody TransferRequest request);
    @GetMapping("/api/notifications/user")
    BankResponse<List<NotificationInfo>> findAllUserNotifications();
}