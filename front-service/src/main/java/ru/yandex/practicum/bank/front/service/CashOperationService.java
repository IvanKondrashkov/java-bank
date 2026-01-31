package ru.yandex.practicum.bank.front.service;

import java.math.BigDecimal;

public interface CashOperationService {
    void deposit(String accountNumber, BigDecimal amount);
    void withdraw(String accountNumber, BigDecimal amount);
}