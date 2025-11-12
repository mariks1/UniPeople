package com.khasanshin.authservice.controller;

import com.khasanshin.authservice.dto.*;
import com.khasanshin.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERVISOR')")
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody CreateUserRequestDto dto) {
        return userService.create(dto);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable("id") UUID id) {
        return userService.get(id);
    }

    @GetMapping
    public Page<UserDto> findAll(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        userService.delete(id);
    }

    @PutMapping("/{id}/roles")
    public UserDto setRoles(@PathVariable("id") UUID id, @Valid @RequestBody UpdateUserRolesRequestDto dto) {
        return userService.setRoles(id, dto);
    }

    @PutMapping("/{id}/managed-departments")
    public UserDto setManagedDepartments(@PathVariable("id") UUID id, @RequestBody Set<UUID> managedDeptIds) {
        return userService.setManagedDepartments(id, managedDeptIds);
    }

    @PostMapping("/{id}/password")
    public void changePassword(@PathVariable("id") UUID id, @Valid @RequestBody ChangePasswordRequestDto dto) {
        userService.changePassword(id, dto);
    }

    @PostMapping("/{id}/enabled")
    public UserDto setEnabled(@PathVariable("id") UUID id, @RequestBody SetEnabledRequestDto dto) {
        return userService.setEnabled(id, dto.isEnabled());
    }
}
