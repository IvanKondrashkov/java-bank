package ru.yandex.practicum.commons.dto.bank;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> BankResponse<T> success(T data) {
        return new BankResponse<>(true, null, data, LocalDateTime.now());
    }

    public static <T> BankResponse<T> success(String message, T data) {
        return new BankResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> BankResponse<T> error(T data) {
        return new BankResponse<>(false, null, data, LocalDateTime.now());
    }

    public static <T> BankResponse<T> error(String message, T data) {
        return new BankResponse<>(false, message, data, LocalDateTime.now());
    }
}