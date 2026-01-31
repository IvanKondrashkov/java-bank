package ru.yandex.practicum.bank.account.service;

import ru.yandex.practicum.bank.account.model.Account;
import ru.yandex.practicum.bank.account.model.UserProfile;
import ru.yandex.practicum.bank.account.model.AccountActions;
import ru.yandex.practicum.bank.account.model.ProfileActions;
import ru.yandex.practicum.commons.dto.bank.NotificationType;

public interface NotificationService {
    void sendNotification(Account account, AccountActions accountActions, NotificationType notificationType);
    void sendNotification(UserProfile userProfile, ProfileActions profileActions, NotificationType notificationType);
}