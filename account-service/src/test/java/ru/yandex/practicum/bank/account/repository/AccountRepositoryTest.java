package ru.yandex.practicum.bank.account.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.yandex.practicum.commons.dto.bank.Currency;
import ru.yandex.practicum.commons.dto.bank.AccountStatus;
import ru.yandex.practicum.bank.account.model.Account;
import ru.yandex.practicum.bank.account.model.UserProfile;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/liquibase-changelog.xml"
})
class AccountRepositoryTest {
    @Autowired
    private AccountRepository repository;
    @Autowired
    private TestEntityManager entityManager;
    private Account account;
    private UserProfile user;

    @BeforeEach
    void setUp() {
        user = UserProfile.builder()
                .username("testUser")
                .firstName("John")
                .lastName("Doe")
                .email("john@mail.ru")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = Account.builder()
                .accountNumber("ACC-001")
                .balance(BigDecimal.ZERO)
                .currency(Currency.RUB)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();
    }

    @AfterEach
    void tearDown() {
        user = null;
        account = null;
    }

    @Test
    void save() {
        entityManager.persist(user);
        entityManager.flush();

        Account accountDb = repository.save(account);
        assertThat(accountDb.getId()).isNotNull();

        entityManager.clear();

        accountDb = repository.findById(accountDb.getId()).orElseThrow();
        assertThat(accountDb.getAccountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(accountDb.getCurrency()).isEqualTo(account.getCurrency());
        assertThat(accountDb.getStatus()).isEqualTo(account.getStatus());
    }

    @Test
    void findByAccountNumber() {
        entityManager.persist(user);
        entityManager.flush();

        entityManager.persist(account);
        entityManager.flush();

        Optional<Account> accountDb = repository.findByAccountNumber(account.getAccountNumber());

        assertThat(accountDb).isPresent();
        assertThat(accountDb.get().getAccountNumber()).isEqualTo(account.getAccountNumber());
    }

    @Test
    void findAllByUser_Username() {
        entityManager.persist(user);
        entityManager.flush();

        entityManager.persist(account);
        entityManager.flush();

        List<Account> accounts = repository.findAllByUser_Username(user.getUsername());

        assertThat(accounts).hasSize(1);
    }

    @Test
    void findByUser_UsernameAndAccountNumber() {
        entityManager.persist(user);
        entityManager.flush();

        entityManager.persist(account);
        entityManager.flush();

        Optional<Account> accountDb = repository.findByUser_UsernameAndAccountNumber(user.getUsername(), account.getAccountNumber());

        assertThat(accountDb).isPresent();
        assertThat(accountDb.get().getUser().getUsername()).isEqualTo(user.getUsername());
        assertThat(accountDb.get().getAccountNumber()).isEqualTo(account.getAccountNumber());
    }

    @Test
    void existsByAccountNumber_isTrue() {
        entityManager.persist(user);
        entityManager.flush();

        entityManager.persist(account);
        entityManager.flush();

        boolean exists = repository.existsByAccountNumber(account.getAccountNumber());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByAccountNumber_isFalse() {
        boolean exists = repository.existsByAccountNumber(account.getAccountNumber());

        assertThat(exists).isFalse();
    }
}