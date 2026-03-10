package ru.yandex.practicum.bank.transfer.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.bank.metrics.BankMetrics;

@Configuration
public class MetricsConfig {

    @Bean
    public BankMetrics bankMetrics(MeterRegistry registry) {
        return new BankMetrics(registry);
    }
}
