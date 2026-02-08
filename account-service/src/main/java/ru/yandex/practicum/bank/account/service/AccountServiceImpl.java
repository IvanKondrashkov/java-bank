package ru.yandex.practicum.bank.account.service;

import java.util.Set;
import java.util.List;
import java.util.EnumSet;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import ru.yandex.practicum.bank.account.model.*;
import ru.yandex.practicum.commons.dto.bank.Currency;
import ru.yandex.practicum.commons.dto.bank.AccountStatus;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.account.response.AccountShortInfo;
import ru.yandex.practicum.commons.dto.account.request.CreateAccountRequest;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;
import ru.yandex.practicum.commons.dto.account.request.CreateOrUpdateUserProfileRequest;
import ru.yandex.practicum.bank.account.mapper.AccountMapper;
import ru.yandex.practicum.bank.account.mapper.UserProfileMapper;
import ru.yandex.practicum.bank.account.repository.AccountRepository;
import ru.yandex.practicum.bank.account.repository.TransactionRepository;
import ru.yandex.practicum.bank.account.repository.UserProfileRepository;
import ru.yandex.practicum.bank.account.util.AccountUtils;
import ru.yandex.practicum.bank.account.exception.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserProfileRepository userProfileRepository;
    private final AccountMapper accountMapper;
    private final UserProfileMapper userProfileMapper;
    private final NotificationService notificationService;
    private static final Set<TransactionType> DEBIT_OPERATIONS = EnumSet.of(
            TransactionType.WITHDRAWAL,
            TransactionType.TRANSFER_OUT,
            TransactionType.FEE
    );

    @Override
    @Transactional(readOnly = true)
    public UserProfileInfo findUserProfileByUsername(String username) {
        UserProfile user = userProfileRepository.findByUsername(username).orElseThrow(
                () -> new EntityNotFoundException(String.format("User profile not found with username: %s", username))
        );
        return userProfileMapper.toInfo(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountShortInfo findByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
                () -> new EntityNotFoundException(String.format("Account not found with number: %s", accountNumber))
        );
        return accountMapper.toShortInfo(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountInfo> findAllByUsername(String username) {
        return accountRepository.findAllByUser_Username(username).stream()
                .map(accountMapper::toInfo)
                .collect(Collectors.toList());
    }

    @Override
    public AccountInfo save(CreateAccountRequest request) {
        UserProfile user = userProfileRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new EntityNotFoundException(String.format("User profile not found with username: %s", request.getUsername()))
        );

        final String accountNumber = AccountUtils.generateAccountNumber();
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new EntityConflictException(String.format("Account number is conflict: %s", accountNumber));
        }

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .currency(Currency.valueOf(request.getCurrency()))
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        accountRepository.save(account);
        notificationService.sendNotification(account, AccountActions.ACCOUNT_CREATED, NotificationType.PUSH);
        return accountMapper.toInfo(account);
    }

    @Override
    public UserProfileInfo createUserProfile(String username, CreateOrUpdateUserProfileRequest request) {
        UserProfile user = userProfileRepository.findByUsername(username).orElseGet(() -> {
            UserProfile newUser = UserProfile.builder()
                    .username(username)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .dateOfBirth(request.getDateOfBirth())
                    .build();
            return userProfileRepository.save(newUser);
        });
        notificationService.sendNotification(user, ProfileActions.PROFILE_CREATED, NotificationType.PUSH);
        return userProfileMapper.toInfo(user);
    }

    @Override
    public UserProfileInfo updateUserProfile(String username, CreateOrUpdateUserProfileRequest request) {
        UserProfile user = userProfileRepository.findByUsername(username).orElseThrow(
                () -> new EntityNotFoundException(String.format("User profile not found with username: %s", username))
        );

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());
        userProfileRepository.save(user);
        notificationService.sendNotification(user, ProfileActions.PROFILE_UPDATED, NotificationType.PUSH);
        return userProfileMapper.toInfo(user);
    }

    @Override
    public AccountInfo processAccountActions(String username, String accountNumber, AccountActions accountActions) {
        Account account = accountRepository.findByUser_UsernameAndAccountNumber(username, accountNumber).orElseThrow(
                () -> new EntityNotFoundException(String.format("Account not found with username: %s", username))
        );

        switch (accountActions) {
            case ACCOUNT_CLOSED -> account.setStatus(AccountStatus.CLOSED);
            case ACCOUNT_BLOCKED -> account.setStatus(AccountStatus.BLOCKED);
        }
        accountRepository.save(account);
        notificationService.sendNotification(account, accountActions, NotificationType.PUSH);
        return accountMapper.toInfo(account);
    }

    @Override
    @Retry(name = "cashService", fallbackMethod = "retryFallback")
    @CircuitBreaker(name = "cashService", fallbackMethod = "processBalanceFallback")
    public AccountInfo processBalance(String accountNumber, UpdateBalanceRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
                () -> new EntityNotFoundException(String.format("Account not found with number: %s", accountNumber))
        );

        boolean isExist = transactionRepository.existsByOperationId(request.getOperationId());
        if (isExist) {
            throw new InvalidAccountException(String.format("Operation already processed: %s", request.getOperationId()));
        }

        if (!account.isActive()) {
            throw new InvalidAccountException(String.format("Account is not active: %s", account.getAccountNumber()));
        }

        BigDecimal balanceBefore = account.getBalance();
        TransactionType type = TransactionType.valueOf(request.getOperationType());
        if (DEBIT_OPERATIONS.contains(type) && !account.hasSufficientFunds(request.getAmount())) {
            throw new InvalidAccountException(String.format("Insufficient funds in account: %s", accountNumber));
        }

        switch (type) {
            case DEPOSIT, TRANSFER_IN -> account.setBalance(account.getBalance().add(request.getAmount()));
            case WITHDRAWAL, TRANSFER_OUT, FEE -> account.setBalance(account.getBalance().subtract(request.getAmount()));
        };

        Transaction transaction = Transaction.builder()
                .accountNumber(accountNumber)
                .amount(request.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(account.getBalance())
                .type(type)
                .description(request.getDescription())
                .operationId(request.getOperationId())
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
        accountRepository.save(account);

        log.info("Balance updated for account {}: {} (version: {})", accountNumber, account.getBalance(), account.getVersion());
        notificationService.sendNotification(account, AccountActions.BALANCE_UPDATED, NotificationType.PUSH);
        return accountMapper.toInfo(account);
    }

    public AccountInfo retryFallback(UpdateBalanceRequest request, Exception e) {
        log.error("All retries exhausted", e);
        throw new InvalidAccountException("Account update balance failed after retries");
    }

    public AccountInfo processBalanceFallback(UpdateBalanceRequest request, Exception e) {
        log.error("Account update balance fallback triggered", e);
        throw new ServiceUnavailableException("Account service temporarily unavailable", e);
    }
}