package ru.yandex.practicum.bank.notification.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.bank.notification.mapper.NotificationMapper;
import ru.yandex.practicum.bank.notification.mapper.NotificationMapperImpl;
import ru.yandex.practicum.bank.notification.repository.NotificationRepository;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.bank.NotificationStatus;
import ru.yandex.practicum.bank.notification.model.Notification;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;
import ru.yandex.practicum.commons.dto.notification.request.NotificationRequest;
import ru.yandex.practicum.bank.notification.exception.EntityNotFoundException;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock
    private NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper = new NotificationMapperImpl();
    private NotificationService notificationService;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(1L)
                .username("testUser")
                .message("Deposit to ACC-001")
                .type(NotificationType.PUSH)
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                notificationMapper
        );
    }

    @AfterEach
    void tearDown() {
        notification = null;
        notificationService = null;
    }

    @Test
    void findById() {
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        NotificationInfo notificationDb = notificationService.findById(notification.getId());

        assertThat(notificationDb).isNotNull();
        assertThat(notificationDb.getId()).isEqualTo(notification.getId());
        assertThat(notificationDb.getUsername()).isEqualTo(notification.getUsername());
        assertThat(notificationDb.getMessage()).isEqualTo(notification.getMessage());
        assertThat(notificationDb.getType()).isEqualTo(notification.getType().name());
        assertThat(notificationDb.getStatus()).isEqualTo(notification.getStatus().name());

        verify(notificationRepository, times(1)).findById(notification.getId());
    }

    @Test
    void findById_throwsEntityNotFoundException() {
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.findById(notification.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Notification not found with id: %s", notification.getId()));

        verify(notificationRepository, times(1)).findById(notification.getId());
    }

    @Test
    void findAllByUsername() {
        when(notificationRepository.findAllByUsername(notification.getUsername())).thenReturn(List.of(notification));

        List<NotificationInfo> notifications = notificationService.findAllByUsername(notification.getUsername());

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getUsername()).isEqualTo(notification.getUsername());
        assertThat(notifications.get(0).getMessage()).isEqualTo(notification.getMessage());

        verify(notificationRepository, times(1)).findAllByUsername(notification.getUsername());
    }

    @Test
    void findAllByUsername_isEmptyList() {
        when(notificationRepository.findAllByUsername(notification.getUsername())).thenReturn(List.of());

        List<NotificationInfo> notifications = notificationService.findAllByUsername(notification.getUsername());

        assertThat(notifications).isEmpty();

        verify(notificationRepository, times(1)).findAllByUsername(notification.getUsername());
    }

    @Test
    void save() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationRequest request = new NotificationRequest(
                notification.getUsername(),
                notification.getMessage(),
                notification.getType().name()
        );

        notificationService.save(request);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}