package ru.yandex.practicum.bank.account.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.yandex.practicum.bank.account.model.Account;
import ru.yandex.practicum.bank.account.model.UserProfile;
import ru.yandex.practicum.bank.account.model.AccountActions;
import ru.yandex.practicum.bank.account.model.ProfileActions;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
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
    public void sendNotification(Account account, AccountActions accountActions, NotificationType notificationType) {
        send(account.getUser().getUsername(), getMessage(account, accountActions), notificationType);
    }

    @Override
    public void sendNotification(UserProfile userProfile, ProfileActions profileActions, NotificationType notificationType) {
        send(userProfile.getUsername(), getMessage(userProfile, profileActions), notificationType);
    }

    private void send(String username, String message, NotificationType notificationType) {
        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setUsername(username);
            notification.setMessage(message);
            notification.setType(notificationType.name());
            rabbitTemplate.convertAndSend(exchangeName, routingKey, notification);
        } catch (Exception e) {
            log.warn("Failed to send notification to queue", e);
        }
    }

    private static String getMessage(Account account, AccountActions accountActions) {
        return switch (accountActions) {
            case ACCOUNT_CREATED -> String.format("Account %s created successfully", account.getAccountNumber());
            case ACCOUNT_CLOSED -> String.format("Account %s closed successfully", account.getAccountNumber());
            case ACCOUNT_BLOCKED -> String.format("Account %s blocked successfully", account.getAccountNumber());
            case BALANCE_UPDATED -> String.format("Balance updated for account %s: %s", account.getAccountNumber(), account.getBalance());
        };
    }

    private static String getMessage(UserProfile userProfile, ProfileActions profileActions) {
        return switch (profileActions) {
            case PROFILE_CREATED -> String.format("User profile %s created successfully", userProfile.getUsername());
            case PROFILE_UPDATED -> String.format("User profile %s updated successfully", userProfile.getUsername());
        };
    }
}