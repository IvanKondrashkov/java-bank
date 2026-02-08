package ru.yandex.practicum.bank.front.exception;

import lombok.Getter;

@Getter
public class BackendApiException extends RuntimeException {
    private final int statusCode;

    public BackendApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public BackendApiException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}