package ru.yandex.practicum.commons.dto.account.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    @NotNull
    @NotBlank
    private String username;
    @NotNull
    private String currency;
}