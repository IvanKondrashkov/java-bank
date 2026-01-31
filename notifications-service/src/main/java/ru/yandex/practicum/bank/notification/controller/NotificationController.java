package ru.yandex.practicum.bank.notification.controller;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import ru.yandex.practicum.commons.dto.bank.BankResponse;
import ru.yandex.practicum.commons.dto.notification.response.NotificationInfo;
import ru.yandex.practicum.bank.notification.service.NotificationService;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankResponse<NotificationInfo>> findById(@PathVariable Long id) {
        NotificationInfo notification = notificationService.findById(id);
        return ResponseEntity.ok(BankResponse.success(notification));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BankResponse<List<NotificationInfo>>> findAllByUsername(@AuthenticationPrincipal Jwt jwt) {
        final String username = jwt.getClaimAsString("preferred_username");
        List<NotificationInfo> notifications = notificationService.findAllByUsername(username);
        return ResponseEntity.ok(BankResponse.success(notifications));
    }
}