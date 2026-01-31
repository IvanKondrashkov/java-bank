package ru.yandex.practicum.bank.cash.service;

import java.util.List;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationInfo;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationShortInfo;
import ru.yandex.practicum.commons.dto.cash.request.CashOperationRequest;

public interface CashService {
    CashOperationShortInfo findById(Long id);
    List<CashOperationShortInfo> findAllByAccountNumber(String accountNumber);
    CashOperationInfo processOperation(CashOperationRequest request);
}