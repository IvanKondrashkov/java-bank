package ru.yandex.practicum.bank.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.bank.account.model.Account;
import ru.yandex.practicum.commons.dto.account.response.AccountInfo;
import ru.yandex.practicum.commons.dto.account.response.AccountShortInfo;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "user.username", target = "user.username")
    @Mapping(source = "user.firstName", target = "user.firstName")
    @Mapping(source = "user.lastName", target = "user.lastName")
    @Mapping(source = "user.email", target = "user.email")
    @Mapping(source = "user.dateOfBirth", target = "user.dateOfBirth")
    AccountInfo toInfo(Account account);

    @Mapping(source = "user.firstName", target = "user.firstName")
    @Mapping(source = "user.lastName", target = "user.lastName")
    @Mapping(source = "user.email", target = "user.email")
    @Mapping(source = "user.dateOfBirth", target = "user.dateOfBirth")
    AccountShortInfo toShortInfo(Account account);
}