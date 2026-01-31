package ru.yandex.practicum.bank.notification.service;

import java.util.List;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.notification.model.Notification;
import ru.yandex.practicum.bank.notification.mapper.NotificationMapper;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.bank.NotificationStatus;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;
import ru.yandex.practicum.commons.dto.notification.request.NotificationRequest;
import ru.yandex.practicum.bank.notification.repository.NotificationRepository;
import ru.yandex.practicum.bank.notification.exception.EntityNotFoundException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public NotificationInfo findById(Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Notification not found with id: %s", id))
        );
        return notificationMapper.toNotificationInfo(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationInfo> findAllByUsername(String username) {
        return notificationRepository.findAllByUsername(username).stream()
                .map(notificationMapper::toNotificationInfo)
                .toList();
    }

    @Override
    public void save(NotificationRequest request) {
        Notification notification = Notification.builder()
                .username(request.getUsername())
                .message(request.getMessage())
                .type(NotificationType.valueOf(request.getType()))
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Notification saved with id={} for username={}", notification.getId(), notification.getUsername());
    }
}