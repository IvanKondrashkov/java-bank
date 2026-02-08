package ru.yandex.practicum.bank.cash.service;

import ru.yandex.practicum.commons.dto.bank.CashOperationType;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;

public interface NotificationService {
    void sendNotification(AccountInfo accountInfo, CashOperationType operationType, NotificationType notificationType);
}