package ru.yandex.practicum.bank.transfer.repository;

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
import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.commons.dto.bank.PaymentStatus;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/liquibase-changelog.xml"
})
class TransferRepositoryTest {
    @Autowired
    private TransferRepository repository;
    @Autowired
    private TestEntityManager entityManager;
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        transfer = Transfer.builder()
                .payerAccountNumber("ACC-001")
                .payeeAccountNumber("ACC-002")
                .amount(new BigDecimal("100.0"))
                .status(PaymentStatus.COMPLETED)
                .description("Transfer from ACC-001 to ACC-002")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        transfer = null;
    }

    @Test
    void save() {
        Transfer transferDb = repository.save(transfer);
        assertThat(transferDb.getId()).isNotNull();

        entityManager.clear();

        transferDb = repository.findById(transferDb.getId()).orElseThrow();
        assertThat(transferDb.getPayerAccountNumber()).isEqualTo(transfer.getPayerAccountNumber());
        assertThat(transferDb.getPayeeAccountNumber()).isEqualTo(transfer.getPayeeAccountNumber());
        assertThat(transferDb.getAmount()).isEqualByComparingTo(transfer.getAmount());
        assertThat(transferDb.getStatus()).isEqualTo(transfer.getStatus());
    }

    @Test
    void findAllByPayerAccountNumber() {
        entityManager.persist(transfer);
        entityManager.flush();

        List<Transfer> transfers = repository.findAllByPayerAccountNumber(transfer.getPayerAccountNumber());

        assertThat(transfers).hasSize(1);
        assertThat(transfers.get(0).getPayerAccountNumber()).isEqualTo(transfer.getPayerAccountNumber());
    }

    @Test
    void findAllByPayerAccountNumber_isEmptyList() {
        List<Transfer> transfers = repository.findAllByPayerAccountNumber(transfer.getPayerAccountNumber());

        assertThat(transfers).isEmpty();
    }

    @Test
    void findAllByPayeeAccountNumber() {
        entityManager.persist(transfer);
        entityManager.flush();

        List<Transfer> transfers = repository.findAllByPayeeAccountNumber(transfer.getPayeeAccountNumber());

        assertThat(transfers).hasSize(1);
        assertThat(transfers.get(0).getPayeeAccountNumber()).isEqualTo(transfer.getPayeeAccountNumber());
    }

    @Test
    void findAllByPayeeAccountNumber_isEmptyList() {
        List<Transfer> transfers = repository.findAllByPayeeAccountNumber(transfer.getPayeeAccountNumber());

        assertThat(transfers).isEmpty();
    }

    @Test
    void count() {
        assertThat(repository.count()).isZero();

        entityManager.persist(transfer);
        entityManager.flush();

        assertThat(repository.count()).isEqualTo(1L);
    }
}