package ru.yandex.practicum.bank.account.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class AccountUtils {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int MAX_LENGTH = 20;

    public static String generateAccountNumber() {
        StringBuilder sb = new StringBuilder(MAX_LENGTH);

        for (int i = 0; i < MAX_LENGTH; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}