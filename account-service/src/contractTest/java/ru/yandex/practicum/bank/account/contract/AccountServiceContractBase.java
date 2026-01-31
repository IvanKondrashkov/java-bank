package ru.yandex.practicum.bank.account.contract;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import ru.yandex.practicum.bank.account.config.TestSecurityConfig;
import ru.yandex.practicum.commons.dto.bank.Currency;
import ru.yandex.practicum.commons.dto.bank.AccountStatus;
import ru.yandex.practicum.bank.account.controller.AccountController;
import ru.yandex.practicum.bank.account.service.AccountService;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;

@Import(TestSecurityConfig.class)
@WebMvcTest(AccountController.class)
@ActiveProfiles("contract-test")
@WithMockUser(authorities = {"ROLE_SERVICE", "accounts.write"})
public abstract class AccountServiceContractBase {
    @Autowired
    protected MockMvc mockMvc;
    @MockitoBean
    protected AccountService accountService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        when(accountService.processBalance(eq("ACC-001"), any(UpdateBalanceRequest.class)))
                .thenAnswer(invocation -> AccountInfo.builder()
                        .id(1L)
                        .accountNumber(invocation.getArgument(0))
                        .balance(new BigDecimal("600.5"))
                        .currency(Currency.RUB.name())
                        .status(AccountStatus.ACTIVE.name())
                        .build());
    }
}