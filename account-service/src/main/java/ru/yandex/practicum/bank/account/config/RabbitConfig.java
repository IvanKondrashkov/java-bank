package ru.yandex.practicum.bank.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitConfig {
    @Value("${bank.notifications.amqp.exchange-name:bank.notifications}")
    private String exchangeName;

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}