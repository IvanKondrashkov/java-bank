package ru.yandex.practicum.bank.account.service;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.bank.account.mapper.AccountMapper;
import ru.yandex.practicum.bank.account.mapper.AccountMapperImpl;
import ru.yandex.practicum.bank.account.mapper.UserProfileMapper;
import ru.yandex.practicum.bank.account.mapper.UserProfileMapperImpl;
import ru.yandex.practicum.bank.account.model.Account;
import ru.yandex.practicum.bank.account.model.UserProfile;
import ru.yandex.practicum.commons.dto.bank.Currency;
import ru.yandex.practicum.commons.dto.bank.AccountStatus;
import ru.yandex.practicum.bank.account.model.AccountActions;
import ru.yandex.practicum.bank.account.model.ProfileActions;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.account.response.AccountShortInfo;
import ru.yandex.practicum.bank.account.repository.AccountRepository;
import ru.yandex.practicum.bank.account.repository.TransactionRepository;
import ru.yandex.practicum.bank.account.repository.UserProfileRepository;
import ru.yandex.practicum.commons.dto.account.request.CreateAccountRequest;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;
import ru.yandex.practicum.commons.dto.account.request.CreateOrUpdateUserProfileRequest;
import ru.yandex.practicum.bank.account.exception.InvalidAccountException;
import ru.yandex.practicum.bank.account.exception.EntityNotFoundException;
import ru.yandex.practicum.bank.metrics.BankMetrics;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private BankMetrics bankMetrics;
    private final AccountMapper accountMapper = new AccountMapperImpl();
    private final UserProfileMapper userProfileMapper = new UserProfileMapperImpl();
    private AccountService accountService;
    private Account account;
    private UserProfile user;

    @BeforeEach
    void setUp() {
        user = UserProfile.builder()
                .id(1L)
                .username("testUser")
                .firstName("John")
                .lastName("Doe")
                .email("john@mail.ru")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(BigDecimal.ZERO)
                .currency(Currency.RUB)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .version(0L)
                .build();
        accountService = new AccountServiceImpl(
                accountRepository,
                transactionRepository,
                userProfileRepository,
                accountMapper,
                userProfileMapper,
                notificationService,
                bankMetrics
        );
    }

    @AfterEach
    void tearDown() {
        user = null;
        account = null;
        accountService = null;
    }

    @Test
    void findUserProfileByUsername() {
        when(userProfileRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserProfileInfo userDb = accountService.findUserProfileByUsername(user.getUsername());

        assertThat(userDb).isNotNull();
        assertThat(userDb.getUsername()).isEqualTo(user.getUsername());
        assertThat(userDb.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userDb.getLastName()).isEqualTo(user.getLastName());
        assertThat(userDb.getEmail()).isEqualTo(user.getEmail());

        verify(userProfileRepository, times(1)).findByUsername(user.getUsername());
    }

    @Test
    void findUserProfileByUsername_throwsEntityNotFoundException() {
        when(userProfileRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findUserProfileByUsername(user.getUsername()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("User profile not found with username: %s", user.getUsername()));

        verify(userProfileRepository, times(1)).findByUsername(user.getUsername());
    }

    @Test
    void findByAccountNumber() {
        when(accountRepository.findByAccountNumber(account.getAccountNumber())).thenReturn(Optional.of(account));

        AccountShortInfo accountDb = accountService.findByAccountNumber(account.getAccountNumber());

        assertThat(accountDb).isNotNull();
        assertThat(accountDb.getAccountNumber()).isEqualTo(account.getAccountNumber());

        verify(accountRepository, times(1)).findByAccountNumber(account.getAccountNumber());
    }

    @Test
    void findByAccountNumber_throwsEntityNotFoundException() {
        when(accountRepository.findByAccountNumber(account.getAccountNumber())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findByAccountNumber(account.getAccountNumber()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Account not found with number: %s", account.getAccountNumber()));

        verify(accountRepository, times(1)).findByAccountNumber(account.getAccountNumber());
    }

    @Test
    void findAllByUsername() {
        when(accountRepository.findAllByUser_Username(user.getUsername())).thenReturn(List.of(account));

        List<AccountInfo> accounts = accountService.findAllByUsername(user.getUsername());

        assertThat(accounts).hasSize(1);

        verify(accountRepository, times(1)).findAllByUser_Username(user.getUsername());
    }

    @Test
    void findAllByUsername_isEmptyList() {
        when(accountRepository.findAllByUser_Username(user.getUsername())).thenReturn(List.of());

        List<AccountInfo> accounts = accountService.findAllByUsername(user.getUsername());

        assertThat(accounts).isEmpty();

        verify(accountRepository, times(1)).findAllByUser_Username(user.getUsername());
    }

    @Test
    void save() {
        CreateAccountRequest request = new CreateAccountRequest(user.getUsername(), "RUB");

        when(userProfileRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);


        AccountInfo accountDb = accountService.save(request);

        assertThat(accountDb).isNotNull();
        assertThat(accountDb.getCurrency()).isEqualTo("RUB");

        verify(userProfileRepository, times(1)).findByUsername(user.getUsername());
        verify(accountRepository, times(1)).existsByAccountNumber(any());
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(notificationService, times(1)).sendNotification(any(Account.class), eq(AccountActions.ACCOUNT_CREATED), any());
    }

    @Test
    void save_throwsEntityNotFoundException() {
        when(userProfileRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        CreateAccountRequest request = new CreateAccountRequest(user.getUsername(), "RUB");

        assertThatThrownBy(() -> accountService.save(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("User profile not found with username: %s", user.getUsername()));

        verify(userProfileRepository, times(1)).findByUsername(user.getUsername());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createUserProfile() {
        when(userProfileRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(user);

        CreateOrUpdateUserProfileRequest request = new CreateOrUpdateUserProfileRequest(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDateOfBirth()
        );

        UserProfileInfo userDb = accountService.createUserProfile(user.getUsername(), request);

        assertThat(userDb).isNotNull();
        assertThat(userDb.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(userDb.getLastName()).isEqualTo(request.getLastName());
        assertThat(userDb.getEmail()).isEqualTo(request.getEmail());
        assertThat(userDb.getDateOfBirth()).isEqualTo(request.getDateOfBirth());

        verify(userProfileRepository, times(1)).findByUsername(user.getUsername());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        verify(notificationService, times(1)).sendNotification(eq(user), eq(ProfileActions.PROFILE_CREATED), any());
    }

    @Test
    void updateUserProfile() {
        when(userProfileRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(user);

        CreateOrUpdateUserProfileRequest request = new CreateOrUpdateUserProfileRequest(
                "Nik",
                "Poll",
                "nik@mail.ru",
                LocalDate.of(1991, 1, 1)
        );

        UserProfileInfo userDb = accountService.updateUserProfile(user.getUsername(), request);

        assertThat(userDb).isNotNull();
        assertThat(userDb.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(userDb.getLastName()).isEqualTo(request.getLastName());
        assertThat(userDb.getEmail()).isEqualTo(request.getEmail());
        assertThat(userDb.getDateOfBirth()).isEqualTo(request.getDateOfBirth());

        verify(userProfileRepository, times(1)).findByUsername(user.getUsername());
        verify(userProfileRepository, times(1)).save(user);
        verify(notificationService, times(1)).sendNotification(eq(user), eq(ProfileActions.PROFILE_UPDATED), any());
    }

    @Test
    void updateUserProfile_throwsEntityNotFoundException() {
        when(userProfileRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        CreateOrUpdateUserProfileRequest request = new CreateOrUpdateUserProfileRequest(
                "Nik",
                "Poll",
                "nik@mail.ru",
                LocalDate.of(1991, 1, 1)
        );

        assertThatThrownBy(() -> accountService.updateUserProfile(user.getUsername(), request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("User profile not found with username: %s", user.getUsername()));

        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void processAccountActions() {
        when(accountRepository.findByUser_UsernameAndAccountNumber(user.getUsername(), account.getAccountNumber())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountInfo accountDb = accountService.processAccountActions(user.getUsername(), account.getAccountNumber(), AccountActions.ACCOUNT_CLOSED);

        assertThat(accountDb).isNotNull();
        assertThat(accountDb.getStatus()).isEqualTo(AccountStatus.CLOSED.name());

        verify(accountRepository, times(1)).findByUser_UsernameAndAccountNumber(user.getUsername(), account.getAccountNumber());
        verify(accountRepository, times(1)).save(account);
        verify(notificationService, times(1)).sendNotification(eq(account), eq(AccountActions.ACCOUNT_CLOSED), any());
    }

    @Test
    void processBalance() {
        when(accountRepository.findByAccountNumber(account.getAccountNumber())).thenReturn(Optional.of(account));
        when(transactionRepository.existsByOperationId(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        UpdateBalanceRequest request = new UpdateBalanceRequest(
                new BigDecimal("100.0"),
                "DEPOSIT",
                UUID.randomUUID().toString(),
                "Deposit to ACC-001"
        );

        AccountInfo accountDb = accountService.processBalance(account.getAccountNumber(), request);

        assertThat(accountDb).isNotNull();
        assertThat(accountDb.getBalance()).isEqualByComparingTo(new BigDecimal("100.0"));

        verify(accountRepository, times(1)).findByAccountNumber(account.getAccountNumber());
        verify(transactionRepository, times(1)).existsByOperationId(anyString());
        verify(transactionRepository, times(1)).save(any());
        verify(notificationService, times(1)).sendNotification(eq(account), eq(AccountActions.BALANCE_UPDATED), any());
    }

    @Test
    void processBalance_processedOperationId_throwsInvalidAccountException() {
        when(accountRepository.findByAccountNumber(account.getAccountNumber())).thenReturn(Optional.of(account));
        when(transactionRepository.existsByOperationId(anyString())).thenReturn(true);

        UpdateBalanceRequest request = new UpdateBalanceRequest(
                new BigDecimal("100.0"),
                "DEPOSIT",
                UUID.randomUUID().toString(),
                "Deposit to ACC-001"
        );

        assertThatThrownBy(() -> accountService.processBalance(account.getAccountNumber(), request))
                .isInstanceOf(InvalidAccountException.class)
                .hasMessageContaining("Operation already processed");

        verify(accountRepository, times(1)).findByAccountNumber(account.getAccountNumber());
        verify(transactionRepository, times(1)).existsByOperationId(anyString());
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processBalance_insufficientFunds_throwsInvalidAccountException() {
        when(accountRepository.findByAccountNumber(account.getAccountNumber())).thenReturn(Optional.of(account));
        when(transactionRepository.existsByOperationId(anyString())).thenReturn(false);

        UpdateBalanceRequest request = new UpdateBalanceRequest(
                new BigDecimal("100.0"),
                "WITHDRAWAL",
                UUID.randomUUID().toString(),
                "Withdrawal from ACC-001"
        );

        assertThatThrownBy(() -> accountService.processBalance(account.getAccountNumber(), request))
                .isInstanceOf(InvalidAccountException.class)
                .hasMessageContaining("Insufficient funds");

        verify(accountRepository, times(1)).findByAccountNumber(account.getAccountNumber());
        verify(transactionRepository, times(1)).existsByOperationId(anyString());
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}