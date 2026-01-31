package ru.yandex.practicum.bank.transfer.exception;

public class EntityConflictException extends RuntimeException {
    public EntityConflictException(String message) {
        super(message);
    }

    public EntityConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}