package ru.yandex.practicum.bank.front.service;

import java.math.BigDecimal;

public interface TransferService {
    void transfer(String payerAccountNumber, String payeeAccountNumber, BigDecimal amount);
}