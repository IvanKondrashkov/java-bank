package ru.yandex.practicum.bank.notification.listener;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import ru.yandex.practicum.bank.notification.service.NotificationService;
import ru.yandex.practicum.commons.dto.notification.request.NotificationRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationService notificationService;

    @RabbitListener(queues = "#{notificationAmqpQueueName}")
    public void handleNotification(NotificationRequest request) {
        log.debug("Received notification event: username={}, type={}", request.getUsername(), request.getType());
        notificationService.save(request);
    }
}