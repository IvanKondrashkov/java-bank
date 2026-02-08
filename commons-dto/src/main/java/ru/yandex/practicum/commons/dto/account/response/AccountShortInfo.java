package ru.yandex.practicum.commons.dto.account.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountShortInfo {
    private String accountNumber;
    private UserProfileShortInfo user;
}