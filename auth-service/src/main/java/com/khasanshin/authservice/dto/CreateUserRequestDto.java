package com.khasanshin.authservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Jacksonized
public class CreateUserRequestDto {

    @NotBlank
    @Size(min = 3, max = 64)
    private final String username;

    @NotBlank
    @Size(min = 6, max = 128)
    private final String password;

    @Schema(description = "Роли пользователя")
    private final Set<String> roles;

    @Schema(description = "Связанный сотрудник (если есть)")
    @JsonProperty("employee_id")
    private final UUID employeeId;

    @Schema(description = "Департаменты, которыми управляет (для DEPT_HEAD)")
    @JsonProperty("managed_dept_ids")
    private final Set<UUID> managedDeptIds;

    @Schema(description = "Включена ли учётка")
    private final Boolean enabled = true;

}
