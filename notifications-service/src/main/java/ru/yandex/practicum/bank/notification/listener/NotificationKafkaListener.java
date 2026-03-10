package ru.yandex.practicum.bank.notification.listener;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.retry.annotation.Backoff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import ru.yandex.practicum.bank.metrics.BankMetrics;
import ru.yandex.practicum.bank.notification.service.NotificationService;
import ru.yandex.practicum.commons.dto.notification.request.NotificationRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaListener {
    private final NotificationService notificationService;
    private final BankMetrics bankMetrics;

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000),
            dltTopicSuffix = ".dlt",
            include = {Exception.class}
    )
    @KafkaListener(topics = "${bank.notifications.kafka.topic:bank.notifications}", groupId = "${spring.kafka.consumer.group-id:notifications-service}")
    public void handleNotification(NotificationRequest request) {
        log.debug("Received notification event: username={}, type={}", request.getUsername(), request.getType());
        notificationService.save(request);
    }

    @DltHandler
    public void handleDlt(NotificationRequest request) {
        log.error("Notification moved to DLT after retries exhausted: username={}, type={}, message={}",
                request.getUsername(),
                request.getType(),
                request.getMessage()
        );
        bankMetrics.recordNotificationSendFailure(request.getUsername());
    }
}