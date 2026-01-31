package ru.yandex.practicum.bank.cash.mapper;

import org.mapstruct.Mapper;
import java.math.BigDecimal;
import ru.yandex.practicum.bank.cash.model.CashOperation;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationInfo;
import ru.yandex.practicum.commons.dto.cash.response.CashOperationShortInfo;

@Mapper(componentModel = "spring")
public interface CashOperationMapper {
    CashOperationInfo toInfo(CashOperation operation, String operationId, BigDecimal newBalance);
    CashOperationShortInfo toShortInfo(CashOperation operation);
}