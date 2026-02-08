package ru.yandex.practicum.bank.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import ru.yandex.practicum.bank.notification.model.Notification;
import ru.yandex.practicum.commons.dto.bank.NotificationStatus;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.yandex.practicum.commons.dto.bank.NotificationType;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/liquibase-changelog.xml"
})
class NotificationRepositoryTest {
    @Autowired
    private NotificationRepository repository;
    @Autowired
    private TestEntityManager entityManager;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .username("testUser")
                .message("Deposit to ACC-001")
                .type(NotificationType.PUSH)
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        notification = null;
    }

    @Test
    void save() {
        Notification notificationDb = repository.save(notification);
        assertThat(notificationDb.getId()).isNotNull();

        entityManager.clear();

        notificationDb = repository.findById(notificationDb.getId()).orElseThrow();
        assertThat(notificationDb.getUsername()).isEqualTo(notification.getUsername());
        assertThat(notificationDb.getMessage()).isEqualTo(notification.getMessage());
        assertThat(notificationDb.getType()).isEqualTo(notification.getType());
        assertThat(notificationDb.getStatus()).isEqualTo(notification.getStatus());
    }

    @Test
    void findByUsername() {
        entityManager.persist(notification);
        entityManager.flush();

        List<Notification> notifications = repository.findAllByUsername(notification.getUsername());

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getUsername()).isEqualTo(notification.getUsername());
    }

    @Test
    void findAllByUsername_isEmptyList() {
        List<Notification> notifications = repository.findAllByUsername(notification.getUsername());

        assertThat(notifications).isEmpty();
    }

    @Test
    void count() {
        assertThat(repository.count()).isZero();

        entityManager.persist(notification);
        entityManager.flush();

        assertThat(repository.count()).isEqualTo(1L);
    }
}