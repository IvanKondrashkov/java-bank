package ru.yandex.practicum.bank.front.service;

import java.util.List;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.bank.front.client.GatewayClient;
import ru.yandex.practicum.bank.front.util.BankResponseUtil;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final GatewayClient gatewayClient;

    @Override
    public List<NotificationInfo> findAllUserNotifications() {
        try {
            BankResponse<List<NotificationInfo>> response = gatewayClient.findAllUserNotifications();
            BankResponseUtil.failIfNotSuccess(response);
            return response.getData();
        } catch (FeignException ex) {
            return Collections.emptyList();
        }
    }
}