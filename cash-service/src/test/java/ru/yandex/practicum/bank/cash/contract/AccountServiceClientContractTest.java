package ru.yandex.practicum.bank.cash.contract;

import java.util.UUID;
import java.math.BigDecimal;
import feign.RequestInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import ru.yandex.practicum.bank.cash.client.AccountServiceClient;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.request.UpdateBalanceRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = AccountServiceClientContractTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@AutoConfigureStubRunner(
        stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = "ru.practicum:account-service:+:stubs:8082"
)
@ActiveProfiles("contract-test")
class AccountServiceClientContractTest {
    @Autowired
    private AccountServiceClient accountServiceClient;

    @Test
    void processBalanceToContract() {
        UpdateBalanceRequest request = new UpdateBalanceRequest(
                new BigDecimal("100.0"),
                "DEPOSIT",
                UUID.randomUUID().toString(),
                "Contract test deposit"
        );

        BankResponse<AccountInfo> response = accountServiceClient.processBalance("ACC-001", request);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getAccountNumber()).isNotBlank();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            LiquibaseAutoConfiguration.class
    })
    @EnableFeignClients(clients = AccountServiceClient.class)
    static class TestConfig {
        @Bean
        public RequestInterceptor contractTestAuthInterceptor() {
            return template -> template.header("Authorization", "Bearer contract-test-token");
        }
    }
}