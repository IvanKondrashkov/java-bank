package ru.yandex.practicum.bank.account.repository;

import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.yandex.practicum.bank.account.model.UserProfile;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/liquibase-changelog.xml"
})
class UserProfileRepositoryTest {
    @Autowired
    private UserProfileRepository repository;
    @Autowired
    private TestEntityManager entityManager;
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
    }

    @AfterEach
    void tearDown() {
        user = null;
    }

    @Test
    void save() {
        UserProfile userDb = repository.save(user);
        assertThat(userDb.getId()).isNotNull();

        entityManager.clear();

        userDb = repository.findById(userDb.getId()).orElseThrow();
        assertThat(userDb.getUsername()).isEqualTo(user.getUsername());
        assertThat(userDb.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userDb.getLastName()).isEqualTo(user.getLastName());
        assertThat(userDb.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findByUsername() {
        entityManager.persist(user);
        entityManager.flush();

        Optional<UserProfile> userDb = repository.findByUsername(user.getUsername());

        assertThat(userDb).isPresent();
        assertThat(userDb.get().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void findByUsername_isEmpty() {
        Optional<UserProfile> userDb = repository.findByUsername(user.getUsername());

        assertThat(userDb).isEmpty();
    }
}