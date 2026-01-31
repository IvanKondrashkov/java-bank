package ru.yandex.practicum.commons.dto.account.response;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileShortInfo {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
}