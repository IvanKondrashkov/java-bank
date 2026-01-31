package ru.yandex.practicum.commons.dto.account.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfo {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserProfileInfo user;
}