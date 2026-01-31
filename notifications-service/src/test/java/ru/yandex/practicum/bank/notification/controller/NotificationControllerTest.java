package ru.yandex.practicum.bank.notification.controller;

import java.util.List;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import ru.yandex.practicum.bank.notification.model.Notification;
import ru.yandex.practicum.commons.dto.bank.NotificationType;
import ru.yandex.practicum.commons.dto.bank.NotificationStatus;
import ru.yandex.practicum.bank.notification.config.TestSecurityConfig;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.yandex.practicum.bank.notification.mapper.NotificationMapper;
import ru.yandex.practicum.bank.notification.service.NotificationService;
import ru.yandex.practicum.bank.notification.mapper.NotificationMapperImpl;
import ru.yandex.practicum.bank.notification.exception.EntityNotFoundException;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@Import({TestSecurityConfig.class, NotificationMapperImpl.class})
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private NotificationMapper notificationMapper;
    @MockitoBean
    private NotificationService notificationService;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(1L)
                .username("testUser")
                .message("Deposit to ACC-001")
                .type(NotificationType.PUSH)
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        notification = null;
    }

    @Test
    @WithMockUser(username = "testUser")
    void findById() throws Exception {
        when(notificationService.findById(notification.getId())).thenReturn(notificationMapper.toNotificationInfo(notification));

        mockMvc.perform(get("/notifications/{id}", notification.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notification.getId()))
                .andExpect(jsonPath("$.data.username").value(notification.getUsername()))
                .andExpect(jsonPath("$.data.message").value(notification.getMessage()))
                .andExpect(jsonPath("$.data.type").value(notification.getType().name()))
                .andExpect(jsonPath("$.data.status").value(notification.getStatus().name()));

        verify(notificationService, times(1)).findById(notification.getId());
    }

    @Test
    @WithMockUser(username = "testUser")
    void findById_throwsEntityNotFoundException() throws Exception {
        when(notificationService.findById(notification.getId())).thenThrow(
                new EntityNotFoundException(String.format("Notification not found with id: %s", notification.getId()))
        );

        mockMvc.perform(get("/notifications/{id}", notification.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.message").value(String.format("Notification not found with id: %s", notification.getId())));

        verify(notificationService, times(1)).findById(notification.getId());
    }

    @Test
    @WithAnonymousUser
    void findById_throwsAccessDeniedException() throws Exception {
        mockMvc.perform(get("/notifications/1")).andExpect(status().isForbidden());
    }

    @Test
    void findAllByUsername() throws Exception {
        when(notificationService.findAllByUsername(notification.getUsername())).thenReturn(List.of(notificationMapper.toNotificationInfo(notification)));

        mockMvc.perform(get("/notifications/user")
                        .with(jwt()
                                .jwt(j -> j.claim("preferred_username", notification.getUsername()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].username").value(notification.getUsername()))
                .andExpect(jsonPath("$.data[0].message").value(notification.getMessage()));

        verify(notificationService, times(1)).findAllByUsername(notification.getUsername());
    }

    @Test
    void findAllByUsername_isEmptyList() throws Exception {
        when(notificationService.findAllByUsername(notification.getUsername())).thenReturn(List.of());

        mockMvc.perform(get("/notifications/user")
                        .with(jwt()
                                .jwt(j -> j.claim("preferred_username", notification.getUsername()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(notificationService, times(1)).findAllByUsername(notification.getUsername());
    }

    @Test
    @WithAnonymousUser
    void findAllByUsername_throwsAccessDeniedException() throws Exception {
        mockMvc.perform(get("/notifications/user")).andExpect(status().isForbidden());
    }
}