package ru.yandex.practicum.commons.dto.transfer.request;

import lombok.*;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotNull
    @NotBlank
    private String payerAccountNumber;
    @NotNull
    @NotBlank
    private String payeeAccountNumber;
    @NotNull
    @Positive
    private BigDecimal amount;
    @NotBlank
    private String description;
}