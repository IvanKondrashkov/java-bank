package ru.yandex.practicum.bank.transfer.service;

import java.util.List;
import ru.yandex.practicum.bank.transfer.model.TransferType;
import ru.yandex.practicum.commons.dto.transfer.response.TransferInfo;
import ru.yandex.practicum.commons.dto.transfer.response.TransferShortInfo;
import ru.yandex.practicum.commons.dto.transfer.request.TransferRequest;


public interface TransferService {
    TransferShortInfo findById(Long id);
    List<TransferShortInfo> findAllByAccountNumber(String accountNumber, TransferType transferType);
    TransferInfo processTransfer(TransferRequest request);
}