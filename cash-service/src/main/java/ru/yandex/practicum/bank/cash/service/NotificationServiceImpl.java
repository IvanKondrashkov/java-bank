package ru.yandex.practicum.bank.cash.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.bank.CashOperationType;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.notification.request.NotificationRequest;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final RabbitTemplate rabbitTemplate;
    @Value("${bank.notifications.amqp.exchange-name:bank.notifications}")
    private String exchangeName;

    @Value("${bank.notifications.amqp.routing-key:notification}")
    private String routingKey;

    @Override
    public void sendNotification(AccountInfo accountInfo, CashOperationType operationType, NotificationType notificationType) {
        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setUsername(accountInfo.getUser().getUsername());
            notification.setMessage(getMessage(accountInfo, operationType));
            notification.setType(notificationType.name());
            rabbitTemplate.convertAndSend(exchangeName, routingKey, notification);
        } catch (Exception e) {
            log.warn("Failed to send notification to queue", e);
        }
    }

    private static String getMessage(AccountInfo accountInfo, CashOperationType operationType) {
        return switch (operationType) {
            case DEPOSIT -> String.format("Account %s deposit successfully, available balance %s", accountInfo.getAccountNumber(), accountInfo.getBalance());
            case WITHDRAWAL -> String.format("Account %s withdrawal successfully, available balance %s", accountInfo.getAccountNumber(), accountInfo.getBalance());
        };
    }
}