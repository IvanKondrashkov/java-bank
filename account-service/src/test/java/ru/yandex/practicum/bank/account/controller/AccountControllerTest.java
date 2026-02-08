package ru.yandex.practicum.bank.account.controller;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.yandex.practicum.bank.account.config.TestSecurityConfig;
import ru.yandex.practicum.bank.account.mapper.AccountMapper;
import ru.yandex.practicum.bank.account.mapper.AccountMapperImpl;
import ru.yandex.practicum.bank.account.mapper.UserProfileMapper;
import ru.yandex.practicum.bank.account.mapper.UserProfileMapperImpl;
import ru.yandex.practicum.bank.account.service.AccountService;
import ru.yandex.practicum.bank.account.model.Account;
import ru.yandex.practicum.bank.account.model.UserProfile;
import ru.yandex.practicum.bank.account.model.AccountActions;
import ru.yandex.practicum.commons.dto.bank.Currency;
import ru.yandex.practicum.commons.dto.bank.AccountStatus;
import ru.yandex.practicum.commons.dto.account.request.CreateAccountRequest;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;
import ru.yandex.practicum.commons.dto.account.request.CreateOrUpdateUserProfileRequest;
import ru.yandex.practicum.bank.account.exception.EntityNotFoundException;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@Import({TestSecurityConfig.class, AccountMapperImpl.class, UserProfileMapperImpl.class})
@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private UserProfileMapper userProfileMapper;
    @MockitoBean
    private AccountService accountService;
    private UserProfile user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = UserProfile.builder()
                .id(1L)
                .username("testUser")
                .firstName("John")
                .lastName("Doe")
                .email("john@mail.ru")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        account = Account.builder()
                .id(1L)
                .accountNumber("ACC-001")
                .balance(BigDecimal.ZERO)
                .currency(Currency.RUB)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();
    }

    @AfterEach
    void tearDown() {
        user = null;
        account = null;
    }

    @Test
    void findUserProfileByUsername() throws Exception {
        when(accountService.findUserProfileByUsername(user.getUsername())).thenReturn(userProfileMapper.toInfo(user));

        mockMvc.perform(get("/accounts/user/profile")
                        .with(jwt()
                                .jwt(j -> j.claim("preferred_username", user.getUsername()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(user.getLastName()));

        verify(accountService, times(1)).findUserProfileByUsername(user.getUsername());
    }

    @Test
    void findAllByUsername() throws Exception {
        when(accountService.findAllByUsername(user.getUsername())).thenReturn(List.of(accountMapper.toInfo(account)));

        mockMvc.perform(get("/accounts/user/all")
                        .with(jwt()
                                .jwt(j -> j.claim("preferred_username", user.getUsername()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].accountNumber").value(account.getAccountNumber()));

        verify(accountService, times(1)).findAllByUsername(user.getUsername());
    }

    @Test
    @WithMockUser(username = "testUser")
    void findByAccountNumber() throws Exception {
        when(accountService.findByAccountNumber(account.getAccountNumber())).thenReturn(accountMapper.toShortInfo(account));

        mockMvc.perform(get("/accounts/number/{accountNumber}", account.getAccountNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(account.getAccountNumber()));

        verify(accountService, times(1)).findByAccountNumber(account.getAccountNumber());
    }

    @Test
    @WithMockUser(username = "testUser")
    void findByAccountNumber_throwsEntityNotFoundException() throws Exception {
        when(accountService.findByAccountNumber(account.getAccountNumber())).thenThrow(
                new EntityNotFoundException(String.format("Account not found with number: %s", account.getAccountNumber()))
        );

        mockMvc.perform(get("/accounts/number/{accountNumber}", account.getAccountNumber()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.message").value(String.format("Account not found with number: %s", account.getAccountNumber())));

        verify(accountService, times(1)).findByAccountNumber(account.getAccountNumber());
    }

    @Test
    @WithAnonymousUser
    void findByAccountNumber_throwsAccessDeniedException() throws Exception {
        mockMvc.perform(get("/accounts/number/ACC-001")).andExpect(status().isForbidden());
    }

    @Test
    void save() throws Exception {
        when(accountService.save(any(CreateAccountRequest.class))).thenReturn(accountMapper.toInfo(account));

        CreateAccountRequest request = new CreateAccountRequest(user.getUsername(), Currency.RUB.name());

        mockMvc.perform(post("/accounts")
                        .with(csrf())
                        .with(jwt()
                                .jwt(j -> j.claim("preferred_username", user.getUsername()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(account.getAccountNumber()))
                .andExpect(jsonPath("$.data.currency").value(account.getCurrency().name()));

        verify(accountService, times(1)).save(any(CreateAccountRequest.class));
    }

    @Test
    void createUserProfile() throws Exception {
        when(accountService.createUserProfile(eq(user.getUsername()), any(CreateOrUpdateUserProfileRequest.class))).thenReturn(userProfileMapper.toInfo(user));

        CreateOrUpdateUserProfileRequest request = new CreateOrUpdateUserProfileRequest(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDateOfBirth()
        );

        mockMvc.perform(post("/accounts/user/profile")
                        .with(csrf())
                        .with(jwt()
                                .jwt(j -> j.claim("preferred_username", user.getUsername()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(user.getLastName()));

        verify(accountService, times(1)).createUserProfile(eq(user.getUsername()), any(CreateOrUpdateUserProfileRequest.class));
    }

    @Test
    void updateUserProfile() throws Exception {
        when(accountService.updateUserProfile(eq(user.getUsername()), any(CreateOrUpdateUserProfileRequest.class))).thenReturn(userProfileMapper.toInfo(user));

        CreateOrUpdateUserProfileRequest request = new CreateOrUpdateUserProfileRequest(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDateOfBirth()
        );

        mockMvc.perform(put("/accounts/user/profile")
                        .with(csrf())
                        .with(jwt()
                                .jwt(j -> j.claim("preferred_username", user.getUsername()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(user.getLastName()));

        verify(accountService, times(1)).updateUserProfile(eq(user.getUsername()), any(CreateOrUpdateUserProfileRequest.class));
    }

    @Test
    void processAccountActions() throws Exception {
        account.setStatus(AccountStatus.CLOSED);
        when(accountService.processAccountActions(user.getUsername(), account.getAccountNumber(), AccountActions.ACCOUNT_CLOSED)).thenReturn(accountMapper.toInfo(account));

        mockMvc.perform(put("/accounts/{accountNumber}", account.getAccountNumber())
                        .with(csrf())
                        .with(jwt()
                                .jwt(j -> j.claim("preferred_username", user.getUsername()))
                                .authorities(new SimpleGrantedAuthority("ROLE_SERVICE"), new SimpleGrantedAuthority("accounts.write")))
                        .param("actions", AccountActions.ACCOUNT_CLOSED.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(AccountStatus.CLOSED.name()));

        verify(accountService, times(1)).processAccountActions(user.getUsername(), account.getAccountNumber(), AccountActions.ACCOUNT_CLOSED);
    }

    @Test
    void processBalance() throws Exception {
        account.setBalance(new BigDecimal("100.0"));
        when(accountService.processBalance(eq(account.getAccountNumber()), any(UpdateBalanceRequest.class))).thenReturn(accountMapper.toInfo(account));

        UpdateBalanceRequest request = new UpdateBalanceRequest(
                new BigDecimal("100.0"),
                "DEPOSIT",
                UUID.randomUUID().toString(),
                "Deposit to ACC-001"
        );

        mockMvc.perform(put("/accounts/{accountNumber}/balance", account.getAccountNumber())
                        .with(csrf())
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_SERVICE"), new SimpleGrantedAuthority("accounts.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(account.getBalance()));

        verify(accountService, times(1)).processBalance(eq(account.getAccountNumber()), any(UpdateBalanceRequest.class));
    }

    @Test
    @WithAnonymousUser
    void processBalance_throwsAccessDeniedException() throws Exception {
        UpdateBalanceRequest request = new UpdateBalanceRequest(
                new BigDecimal("100.0"),
                "DEPOSIT",
                UUID.randomUUID().toString(),
                "Deposit to ACC-001"
        );

        mockMvc.perform(put("/accounts/ACC-001/balance")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}