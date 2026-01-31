package ru.yandex.practicum.bank.transfer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;

@FeignClient(name = "account-service", path = "/accounts")
public interface AccountServiceClient {
    @PutMapping("/{accountNumber}/balance")
    BankResponse<AccountInfo> processBalance(@PathVariable String accountNumber, @RequestBody UpdateBalanceRequest updateBalanceRequest);
}