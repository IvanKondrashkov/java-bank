package ru.yandex.practicum.commons.dto.account.request;

import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
import ru.yandex.practicum.commons.annotations.AdultConstraint;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateUserProfileRequest {
    @NotNull
    @NotBlank
    private String firstName;
    @NotNull
    @NotBlank
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @Past
    @AdultConstraint
    private LocalDate dateOfBirth;
}