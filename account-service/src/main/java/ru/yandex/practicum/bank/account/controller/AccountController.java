package ru.yandex.practicum.bank.account.controller;

import java.util.List;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import ru.yandex.practicum.bank.account.model.AccountActions;
import ru.yandex.practicum.bank.account.service.AccountService;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.response.AccountShortInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.account.request.CreateAccountRequest;
import ru.yandex.practicum.commons.dto.account.request.CreateOrUpdateUserProfileRequest;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;

@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/user/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankResponse<UserProfileInfo>> findUserProfileByUsername(@AuthenticationPrincipal Jwt jwt) {
        final String username = jwt.getClaimAsString("preferred_username");
        UserProfileInfo user = accountService.findUserProfileByUsername(username);
        return ResponseEntity.ok(BankResponse.success(user));
    }

    @GetMapping("/user/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankResponse<List<AccountInfo>>> findAllByUsername(@AuthenticationPrincipal Jwt jwt) {
        final String username = jwt.getClaimAsString("preferred_username");
        List<AccountInfo> accounts = accountService.findAllByUsername(username);
        return ResponseEntity.ok(BankResponse.success(accounts));
    }

    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankResponse<AccountShortInfo>> findByAccountNumber(@PathVariable String accountNumber) {
        AccountShortInfo account = accountService.findByAccountNumber(accountNumber);
        return ResponseEntity.ok(BankResponse.success(account));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') || (hasRole('SERVICE') && hasAuthority('accounts.write'))")
    public ResponseEntity<BankResponse<AccountInfo>> save(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateAccountRequest request) {
        String username = jwt.getClaimAsString("preferred_username");
        CreateAccountRequest createRequest = (username != null && !username.isBlank())
                ? new CreateAccountRequest(username, request.getCurrency())
                : request;
        AccountInfo account = accountService.save(createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(BankResponse.success(account));
    }

    @PostMapping("/user/profile")
    @PreAuthorize("hasAnyRole('USER', 'SERVICE')")
    public ResponseEntity<BankResponse<UserProfileInfo>> createUserProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateOrUpdateUserProfileRequest request) {
        final String username = jwt.getClaimAsString("preferred_username");
        UserProfileInfo user = accountService.createUserProfile(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BankResponse.success(user));
    }

    @PutMapping("/user/profile")
    @PreAuthorize("hasAnyRole('USER', 'SERVICE')")
    public ResponseEntity<BankResponse<UserProfileInfo>> updateUserProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateOrUpdateUserProfileRequest request) {
        final String username = jwt.getClaimAsString("preferred_username");
        UserProfileInfo user = accountService.updateUserProfile(username, request);
        return ResponseEntity.ok(BankResponse.success(user));
    }

    @PutMapping("/{accountNumber}")
    @PreAuthorize("hasRole('SERVICE') && hasAuthority('accounts.write')")
    public ResponseEntity<BankResponse<AccountInfo>> processAccountActions(@AuthenticationPrincipal Jwt jwt, @PathVariable String accountNumber, @RequestParam AccountActions actions) {
        final String username = jwt.getClaimAsString("preferred_username");
        AccountInfo account = accountService.processAccountActions(username, accountNumber, actions);
        return ResponseEntity.ok(BankResponse.success(account));
    }

    @PutMapping("/{accountNumber}/balance")
    @PreAuthorize("hasRole('SERVICE') && hasAuthority('accounts.write')")
    public ResponseEntity<BankResponse<AccountInfo>> processBalance(@PathVariable String accountNumber, @RequestBody UpdateBalanceRequest request) {
        AccountInfo account = accountService.processBalance(accountNumber, request);
        return ResponseEntity.ok(BankResponse.success(account));
    }
}