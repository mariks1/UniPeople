package com.khasanshin.authservice.mapper;

import com.khasanshin.authservice.dto.CreateUserRequestDto;
import com.khasanshin.authservice.dto.UpdateUserRolesRequestDto;
import com.khasanshin.authservice.dto.UserDto;
import com.khasanshin.authservice.entity.AppUser;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(AppUser appUser);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) // пароль выставим в сервисе
    @Mapping(target = "enabled", expression = "java(Boolean.TRUE.equals(src.getEnabled()))")
    AppUser toEntity(CreateUserRequestDto src);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "passwordHash", ignore = true)
    void updateRoles(UpdateUserRolesRequestDto src, @MappingTarget AppUser target);

}
