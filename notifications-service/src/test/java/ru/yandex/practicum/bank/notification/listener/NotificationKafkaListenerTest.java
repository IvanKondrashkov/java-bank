package ru.yandex.practicum.bank.notification.listener;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.bank.notification.service.NotificationService;
import ru.yandex.practicum.commons.dto.notification.request.NotificationRequest;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaListenerTest {
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private NotificationKafkaListener listener;

    @Test
    void handleNotification() {
        NotificationRequest request = new NotificationRequest("testUser", "Balance updated", "PUSH");

        listener.handleNotification(request);

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("testUser");
        assertThat(captor.getValue().getMessage()).isEqualTo("Balance updated");
        assertThat(captor.getValue().getType()).isEqualTo("PUSH");
    }

    @Test
    void handleDlt() {
        NotificationRequest request = new NotificationRequest("testUser", "Failed message", "PUSH");
        listener.handleDlt(request);
    }
}