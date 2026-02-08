package ru.yandex.practicum.commons.dto.cash.request;

import lombok.*;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashOperationRequest {
    @NotNull
    @NotBlank
    private String accountNumber;
    @NotNull
    @Positive
    private BigDecimal amount;
    @NotNull
    @NotBlank
    private String operationType;
    @NotBlank
    private String description;
}