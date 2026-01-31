package ru.yandex.practicum.commons.dto.cash.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashOperationShortInfo {
    private Long id;
    private String accountNumber;
    private BigDecimal amount;
    private String status;
}