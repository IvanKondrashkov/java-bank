package ru.yandex.practicum.bank.transfer.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import ru.yandex.practicum.bank.transfer.model.TransferType;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.notification.request.NotificationRequest;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;
    @Value("${bank.notifications.kafka.topic:bank.notifications}")
    private String topic;

    @Override
    public void sendNotification(AccountInfo accountInfo, TransferType transferType, NotificationType notificationType) {
        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setUsername(accountInfo.getUser().getUsername());
            notification.setMessage(getMessage(accountInfo, transferType));
            notification.setType(notificationType.name());
            kafkaTemplate.send(topic, accountInfo.getUser().getUsername(), notification);
        } catch (Exception e) {
            log.warn("Failed to send notification to Kafka", e);
        }
    }

    private static String getMessage(AccountInfo accountInfo, TransferType transferType) {
        return switch (transferType) {
            case TRANSFER_IN -> String.format("Account %s deposit successfully, available balance %s", accountInfo.getAccountNumber(), accountInfo.getBalance());
            case TRANSFER_OUT -> String.format("Account %s withdrawal successfully, available balance %s", accountInfo.getAccountNumber(), accountInfo.getBalance());
        };
    }
}