package ru.yandex.practicum.bank.front.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.bank.ErrorResponse;
import feign.FeignException;
import ru.yandex.practicum.bank.front.exception.BackendApiException;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class BankResponseUtil {
    public static <T> void failIfNotSuccess(BankResponse<T> response) {
        if (!response.isSuccess()) {
            if (response.getData() instanceof ErrorResponse errorResponse) {
                throw new BackendApiException(errorResponse.getMessage(), errorResponse.getStatus());
            }
        }
    }

    public static BackendApiException toBackendApiException(FeignException exception, String defaultMessage) {
        String message = exception.contentUTF8();
        if (message == null || message.isBlank()) {
            message = defaultMessage;
        }
        return new BackendApiException(message, exception.status());
    }
}