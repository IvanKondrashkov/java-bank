package ru.yandex.practicum.bank.transfer.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.bank.transfer.model.Transfer;
import ru.yandex.practicum.commons.dto.transfer.response.TransferInfo;
import ru.yandex.practicum.commons.dto.transfer.response.TransferShortInfo;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    TransferInfo toTransferInfo(Transfer transfer);
    TransferShortInfo toTransferShortInfo(Transfer transfer);
}