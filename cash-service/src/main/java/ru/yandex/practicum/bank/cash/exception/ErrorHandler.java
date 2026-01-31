package ru.yandex.practicum.bank.cash.exception;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.bank.ErrorResponse;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BankResponse<ErrorResponse>> handleAccessDeniedException(final AccessDeniedException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BankResponse.error(errorResponse));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BankResponse<ErrorResponse>> handleEntityNotFoundException(final EntityNotFoundException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BankResponse.error(errorResponse));
    }
    
    @ExceptionHandler(EntityConflictException.class)
    public ResponseEntity<BankResponse<ErrorResponse>> handleEntityConflictException(final EntityConflictException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(BankResponse.error(errorResponse));
    }

    @ExceptionHandler(InvalidCashOperationException.class)
    public ResponseEntity<BankResponse<ErrorResponse>> handleInvalidCashOperationException(final InvalidCashOperationException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BankResponse.error(errorResponse));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BankResponse<ErrorResponse>> handleRuntimeException(final RuntimeException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(e.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BankResponse.error(errorResponse));
    }
}