package ru.yandex.practicum.bank.front.service;

import java.util.List;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;

public interface NotificationService {
    List<NotificationInfo> findAllUserNotifications();
}