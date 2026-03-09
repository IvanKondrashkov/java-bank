package ru.yandex.practicum.bank.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Value("${bank.notifications.kafka.topic:bank.notifications}")
    private String notificationsTopic;
    @Value("${bank.notifications.kafka.dlt-topic:bank.notifications.dlt}")
    private String notificationsDltTopic;

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(notificationsTopic)
                .partitions(2)
                .replicas(2)
                .build();
    }

    @Bean
    public NewTopic notificationsDltTopic() {
        return TopicBuilder.name(notificationsDltTopic)
                .partitions(2)
                .replicas(2)
                .build();
    }
}