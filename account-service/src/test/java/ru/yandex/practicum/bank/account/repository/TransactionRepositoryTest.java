package ru.yandex.practicum.bank.account.repository;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.yandex.practicum.bank.account.model.Transaction;
import ru.yandex.practicum.bank.account.model.TransactionType;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/liquibase-changelog.xml"
})
class TransactionRepositoryTest {
    @Autowired
    private TransactionRepository repository;
    @Autowired
    private TestEntityManager entityManager;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .accountNumber("ACC-001")
                .amount(new BigDecimal("100.0"))
                .balanceBefore(BigDecimal.ZERO)
                .balanceAfter(new BigDecimal("100.0"))
                .type(TransactionType.DEPOSIT)
                .description("Deposit to ACC-001")
                .operationId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        transaction = null;
    }

    @Test
    void save() {
        Transaction transactionDb = repository.save(transaction);
        assertThat(transactionDb.getId()).isNotNull();

        entityManager.clear();

        transactionDb = repository.findById(transactionDb.getId()).orElseThrow();
        assertThat(transactionDb.getAccountNumber()).isEqualTo(transaction.getAccountNumber());
        assertThat(transactionDb.getOperationId()).isEqualTo(transaction.getOperationId());
        assertThat(transactionDb.getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void existsByOperationIdIsFalse_isTrue() {
        entityManager.persist(transaction);
        entityManager.flush();

        boolean exists = repository.existsByOperationId(transaction.getOperationId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByOperationId_isFalse() {
        boolean exists = repository.existsByOperationId(UUID.randomUUID().toString());

        assertThat(exists).isFalse();
    }
}