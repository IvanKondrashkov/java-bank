package ru.yandex.practicum.commons.dto.notification.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    @NotNull
    @NotBlank
    private String username;
    @NotNull
    @NotBlank
    private String message;
    @NotNull
    @NotBlank
    private String type;
}