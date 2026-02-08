package ru.yandex.practicum.commons.dto.notification.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationInfo {
    private Long id;
    private String username;
    private String message;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}