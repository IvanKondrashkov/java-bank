package ru.yandex.practicum.bank.cash.repository;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.yandex.practicum.bank.cash.model.CashOperation;
import ru.yandex.practicum.commons.dto.bank.PaymentStatus;
import ru.yandex.practicum.commons.dto.bank.CashOperationType;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/liquibase-changelog.xml"
})
class CashOperationRepositoryTest {
    @Autowired
    private CashOperationRepository repository;
    @Autowired
    private TestEntityManager entityManager;
    private CashOperation operation;

    @BeforeEach
    void setUp() {
        operation = CashOperation.builder()
                .accountNumber("ACC-001")
                .amount(new BigDecimal("100.0"))
                .type(CashOperationType.DEPOSIT)
                .status(PaymentStatus.COMPLETED)
                .description("Deposit to ACC-001")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        operation = null;
    }

    @Test
    void save() {
        CashOperation operationDb = repository.save(operation);
        assertThat(operationDb.getId()).isNotNull();

        entityManager.clear();

        operationDb = repository.findById(operationDb.getId()).orElseThrow();
        assertThat(operationDb.getAccountNumber()).isEqualTo(operation.getAccountNumber());
        assertThat(operationDb.getAmount()).isEqualByComparingTo(operation.getAmount());
        assertThat(operationDb.getType()).isEqualTo(operation.getType());
        assertThat(operationDb.getStatus()).isEqualTo(operation.getStatus());
    }

    @Test
    void findByAccountNumber() {
        entityManager.persist(operation);
        entityManager.flush();

        List<CashOperation> operations = repository.findByAccountNumber(operation.getAccountNumber());

        assertThat(operations).hasSize(1);
        assertThat(operations.get(0).getAccountNumber()).isEqualTo(operation.getAccountNumber());
    }

    @Test
    void findByAccountNumber_isEmptyList() {
        List<CashOperation> operations = repository.findByAccountNumber(operation.getAccountNumber());

        assertThat(operations).isEmpty();
    }

    @Test
    void count() {
        assertThat(repository.count()).isZero();

        entityManager.persist(operation);
        entityManager.flush();

        assertThat(repository.count()).isOne();
    }
}