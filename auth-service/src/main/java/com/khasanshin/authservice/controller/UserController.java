package com.khasanshin.authservice.controller;

import com.khasanshin.authservice.application.UserUseCase;
import com.khasanshin.authservice.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERVISOR')")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequestDto dto) {
        UserDto created = userUseCase.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> get(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(userUseCase.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> findAll(@ParameterObject Pageable pageable) {
        Page<UserDto> page = userUseCase.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        userUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<UserDto> setRoles(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRolesRequestDto dto
    ) {
        UserDto updated = userUseCase.setRoles(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/managed-departments")
    public ResponseEntity<UserDto> setManagedDepartments(
            @PathVariable("id") UUID id,
            @RequestBody Set<UUID> managedDeptIds
    ) {
        UserDto updated = userUseCase.setManagedDepartments(id, managedDeptIds);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ChangePasswordRequestDto dto
    ) {
        userUseCase.changePassword(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enabled")
    public ResponseEntity<UserDto> setEnabled(
            @PathVariable("id") UUID id,
            @RequestBody SetEnabledRequestDto dto
    ) {
        UserDto updated = userUseCase.setEnabled(id, dto.isEnabled());
        return ResponseEntity.ok(updated);
    }
}
