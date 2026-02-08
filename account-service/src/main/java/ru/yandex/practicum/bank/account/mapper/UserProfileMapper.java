package ru.yandex.practicum.bank.account.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.bank.account.model.UserProfile;
import ru.yandex.practicum.commons.dto.account.response.UserProfileInfo;
import ru.yandex.practicum.commons.dto.account.response.UserProfileShortInfo;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfileInfo toInfo(UserProfile userProfile);
    UserProfileShortInfo toShortInfo(UserProfile userProfile);
}