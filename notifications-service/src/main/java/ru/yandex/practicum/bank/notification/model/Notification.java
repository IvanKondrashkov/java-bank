package ru.yandex.practicum.bank.notification.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.bank.NotificationStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications", schema = "svc_notifications")
@EqualsAndHashCode(of = "id")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String username;
    @Column(nullable = false, length = 1000)
    private String message;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}