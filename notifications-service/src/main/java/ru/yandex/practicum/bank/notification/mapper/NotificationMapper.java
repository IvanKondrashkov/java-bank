package ru.yandex.practicum.bank.notification.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.bank.notification.model.Notification;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationInfo toNotificationInfo(Notification notification);
}