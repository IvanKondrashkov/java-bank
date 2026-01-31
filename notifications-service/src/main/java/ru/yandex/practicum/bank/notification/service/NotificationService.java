package ru.yandex.practicum.bank.notification.service;

import java.util.List;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;
import ru.yandex.practicum.commons.dto.notification.request.NotificationRequest;

public interface NotificationService {
    NotificationInfo findById(Long id);
    List<NotificationInfo> findAllByUsername(String username);
    void save(NotificationRequest request);
}