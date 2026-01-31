package ru.yandex.practicum.commons.dto.account.request;

import lombok.*;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBalanceRequest {
    @NotNull
    @Positive
    private BigDecimal amount;
    @NotNull
    @NotBlank
    private String operationType;
    @NotNull
    @NotBlank
    private String operationId;
    @NotBlank
    private String description;
}