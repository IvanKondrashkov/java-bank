package ru.yandex.practicum.bank.metrics;

import lombok.RequiredArgsConstructor;
import io.micrometer.core.instrument.MeterRegistry;

@RequiredArgsConstructor
public class BankMetrics {
    private final MeterRegistry registry;
    public void recordCashFailure(String type, String login) {
        registry.counter("bank.cash.failures", "login", login, "type", type).increment();
    }
    public void recordTransferFailure(String type, String login) {
        registry.counter("bank.transfer.failures", "login", login, "type", type).increment();
    }
    public void recordNotificationSendFailure(String login) {
        registry.counter("bank.notification.send.failures", "login", login).increment();
    }
}