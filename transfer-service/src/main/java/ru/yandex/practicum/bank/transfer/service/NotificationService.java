package ru.yandex.practicum.bank.transfer.service;

import ru.yandex.practicum.bank.transfer.model.TransferType;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;

public interface NotificationService {
    void sendNotification(AccountInfo accountInfo, TransferType transferType, NotificationType notificationType);
}