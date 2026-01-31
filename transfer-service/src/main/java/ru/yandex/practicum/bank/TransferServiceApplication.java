package ru.yandex.practicum.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@EnableFeignClients
@SpringBootApplication
public class TransferServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }
}