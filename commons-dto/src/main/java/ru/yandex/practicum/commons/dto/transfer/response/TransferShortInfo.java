package ru.yandex.practicum.commons.dto.transfer.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferShortInfo {
    private Long id;
    private String payerAccountNumber;
    private String payeeAccountNumber;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
}