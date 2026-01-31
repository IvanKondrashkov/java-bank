package ru.yandex.practicum.bank.front.controller;

import java.util.Map;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.bank.front.config.TestSecurityConfig;
import ru.yandex.practicum.bank.front.service.AccountService;
import ru.yandex.practicum.bank.front.service.TransferService;
import ru.yandex.practicum.bank.front.service.CashOperationService;
import ru.yandex.practicum.bank.front.service.NotificationService;
import ru.yandex.practicum.commons.dto.bank.Currency;
import ru.yandex.practicum.commons.dto.bank.AccountStatus;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.bank.NotificationStatus;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;
import ru.yandex.practicum.bank.front.exception.BackendApiException;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@Import(TestSecurityConfig.class)
@WebMvcTest(MainController.class)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false"
})
class MainControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private CashOperationService cashOperationService;
    @MockitoBean
    private TransferService transferService;
    @MockitoBean
    private NotificationService notificationService;
    private UserProfileInfo user;
    private AccountInfo account;
    private NotificationInfo notification;

    @BeforeEach
    void setUp() {
        user = UserProfileInfo.builder()
                .username("testUser")
                .firstName("John")
                .lastName("Doe")
                .email("john@mail.ru")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        account = AccountInfo.builder()
                .accountNumber("ACC-001")
                .balance(BigDecimal.ZERO)
                .currency(Currency.RUB.name())
                .status(AccountStatus.ACTIVE.name())
                .user(user)
                .build();
        notification = NotificationInfo.builder()
                .id(1L)
                .username("testUser")
                .message("Deposit to ACC-001")
                .type(NotificationType.PUSH.name())
                .status(NotificationStatus.SENT.name())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        user = null;
        account = null;
        notification = null;
    }

    @Test
    @WithMockUser(username = "testUser")
    void main() throws Exception {
        Map<String, BigDecimal> totalBalance = Map.of("RUB", new BigDecimal("100.0"));

        when(accountService.findUserProfileByUsername()).thenReturn(user);
        when(accountService.findAllUserAccounts()).thenReturn(List.of(account));
        when(accountService.calculateBalanceByCurrency()).thenReturn(totalBalance);
        when(notificationService.findAllUserNotifications()).thenReturn(List.of(notification));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("profile", user))
                .andExpect(model().attribute("accounts", List.of(account)))
                .andExpect(model().attribute("totalBalance", totalBalance))
                .andExpect(model().attribute("notifications", List.of(notification)));

        verify(accountService, times(1)).findUserProfileByUsername();
        verify(accountService, times(1)).findAllUserAccounts();
        verify(accountService, times(1)).calculateBalanceByCurrency();
        verify(notificationService, times(1)).findAllUserNotifications();
    }

    @Test
    @WithAnonymousUser
    void main_throwsAccessDeniedException() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testUser")
    void createUserProfile() throws Exception {
        when(accountService.findUserProfileByUsername()).thenReturn(null);
        doNothing().when(accountService).createUserProfile(any());

        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("firstName", user.getFirstName())
                        .param("lastName", user.getLastName())
                        .param("email", user.getEmail())
                        .param("dateOfBirth", user.getDateOfBirth().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Профиль создан."));

        verify(accountService, times(1)).findUserProfileByUsername();
        verify(accountService, times(1)).createUserProfile(any());
    }

    @Test
    @WithMockUser(username = "testUser")
    void updateUserProfile() throws Exception {
        when(accountService.findUserProfileByUsername()).thenReturn(user);
        doNothing().when(accountService).updateUserProfile(any());

        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("firstName", user.getFirstName())
                        .param("lastName", user.getLastName())
                        .param("email", user.getEmail())
                        .param("dateOfBirth", user.getDateOfBirth().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Данные сохранены."));

        verify(accountService, times(1)).findUserProfileByUsername();
        verify(accountService, times(1)).updateUserProfile(any());
    }

    @Test
    @WithMockUser(username = "testUser")
    void createOrUpdateUserProfile_validationError() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(csrf())
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", "invalid-email")
                        .param("dateOfBirth", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Ошибка формы обновления профиля."));

        verify(accountService, never()).createUserProfile(any());
        verify(accountService, never()).updateUserProfile(any());
    }

    @Test
    @WithMockUser(username = "testUser")
    void deposit() throws Exception {
        doNothing().when(cashOperationService).deposit(eq(account.getAccountNumber()), any(BigDecimal.class));

        mockMvc.perform(post("/cash/deposit")
                        .with(csrf())
                        .param("accountNumber", account.getAccountNumber())
                        .param("amount", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Счёт пополнен на 100.0."));

        verify(cashOperationService, times(1)).deposit(eq(account.getAccountNumber()), any(BigDecimal.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void deposit_throwsBackendApiException() throws Exception {
        doThrow(new BackendApiException("Сервис не доступен", 400)).when(cashOperationService).deposit(eq(account.getAccountNumber()), any());

        mockMvc.perform(post("/cash/deposit")
                        .with(csrf())
                        .param("accountNumber", account.getAccountNumber())
                        .param("amount", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Сервис не доступен"));

        verify(cashOperationService, times(1)).deposit(eq(account.getAccountNumber()), any());
    }

    @Test
    @WithAnonymousUser
    void deposit_throwsAccessDeniedException() throws Exception {
        mockMvc.perform(post("/cash/deposit")
                        .with(csrf())
                        .param("accountNumber", account.getAccountNumber())
                        .param("amount", "100.0"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cashOperationService);
    }

    @Test
    @WithMockUser(username = "testUser")
    void withdraw() throws Exception {
        doNothing().when(cashOperationService).withdraw(eq(account.getAccountNumber()), any(BigDecimal.class));

        mockMvc.perform(post("/cash/withdraw")
                        .with(csrf())
                        .param("accountNumber", account.getAccountNumber())
                        .param("amount", "50.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Со счёта снято 50.0."));

        verify(cashOperationService, times(1)).withdraw(eq(account.getAccountNumber()), any(BigDecimal.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void withdraw_throwsBackendApiException() throws Exception {
        doThrow(new BackendApiException("Недостаточно средств", 400)).when(cashOperationService).withdraw(eq(account.getAccountNumber()), any());

        mockMvc.perform(post("/cash/withdraw")
                        .with(csrf())
                        .param("accountNumber", account.getAccountNumber())
                        .param("amount", "50.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Недостаточно средств"));

        verify(cashOperationService, times(1)).withdraw(eq(account.getAccountNumber()), any());
    }

    @Test
    @WithAnonymousUser
    void withdraw_throwsAccessDeniedException() throws Exception {
        mockMvc.perform(post("/cash/withdraw")
                        .with(csrf())
                        .param("accountNumber", account.getAccountNumber())
                        .param("amount", "50.0"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cashOperationService);
    }

    @Test
    @WithMockUser(username = "testUser")
    void transfer() throws Exception {
        doNothing().when(transferService).transfer(eq(account.getAccountNumber()), eq("ACC-002"), any(BigDecimal.class));

        mockMvc.perform(post("/transfer")
                        .with(csrf())
                        .param("payerAccountNumber", account.getAccountNumber())
                        .param("payeeAccountNumber", "ACC-002")
                        .param("amount", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Перевод выполнен."));

        verify(transferService, times(1)).transfer(eq(account.getAccountNumber()), eq("ACC-002"), any(BigDecimal.class));
    }

    @Test
    @WithAnonymousUser
    void transfer_throwsAccessDeniedException() throws Exception {
        mockMvc.perform(post("/transfer")
                        .with(csrf())
                        .param("payerAccountNumber", account.getAccountNumber())
                        .param("payeeAccountNumber", "ACC-002")
                        .param("amount", "100.0"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cashOperationService);
    }

    @Test
    @WithMockUser(username = "testUser")
    void createAccount() throws Exception {
        when(accountService.findUserProfileByUsername()).thenReturn(user);
        doNothing().when(accountService).createAccount(eq(user.getUsername()), eq(Currency.RUB.name()));

        mockMvc.perform(post("/accounts")
                        .with(csrf())
                        .param("currency", Currency.RUB.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Счёт создан."));

        verify(accountService, times(1)).findUserProfileByUsername();
        verify(accountService, times(1)).createAccount(eq(user.getUsername()), eq(Currency.RUB.name()));
    }

    @Test
    @WithMockUser(username = "testUser")
    void createAccount_withoutUserProfile() throws Exception {
        when(accountService.findUserProfileByUsername()).thenReturn(null);

        mockMvc.perform(post("/accounts")
                        .with(csrf())
                        .param("currency", Currency.RUB.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("error", "Сначала заполните данные профиля."));

        verify(accountService, times(1)).findUserProfileByUsername();
        verify(accountService, never()).createAccount(any(), any());
    }
}