package ru.yandex.practicum.bank.account.config;

import io.micrometer.core.instrument.MeterRegistry;
import ru.yandex.practicum.bank.metrics.BankMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public BankMetrics bankMetrics(MeterRegistry registry) {
        return new BankMetrics(registry);
    }
}