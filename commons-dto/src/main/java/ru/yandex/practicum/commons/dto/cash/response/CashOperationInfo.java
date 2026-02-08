package ru.yandex.practicum.commons.dto.cash.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashOperationInfo {
    private Long id;
    private String operationId;
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String status;
}