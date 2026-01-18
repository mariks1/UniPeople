package com.khasanshin.authservice.application;

import com.khasanshin.authservice.dto.ChangePasswordRequestDto;
import com.khasanshin.authservice.dto.CreateUserRequestDto;
import com.khasanshin.authservice.dto.UpdateUserRolesRequestDto;
import com.khasanshin.authservice.dto.UserDto;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserUseCase {
    UserDto create(CreateUserRequestDto dto);
    UserDto get(UUID id);
    Page<UserDto> findAll(Pageable pageable);
    void delete(UUID id);
    UserDto setRoles(UUID id, UpdateUserRolesRequestDto dto);
    UserDto setManagedDepartments(UUID id, Set<UUID> managedDeptIds);
    void changePassword(UUID id, ChangePasswordRequestDto dto);
    UserDto setEnabled(UUID id, boolean enabled);
}
