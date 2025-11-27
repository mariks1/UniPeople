package com.khasanshin.authservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.authservice.controller.UserController;
import com.khasanshin.authservice.dto.*;
import com.khasanshin.authservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = UserController.class)
@Import(UserControllerTest.MethodSec.class)
class UserControllerTest {


    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSec { }

    @MockitoBean
    UserService userService;

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void endpoints_requireSupervisorRole() throws Exception {
        mvc.perform(get("/api/v1/auth/users")).andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void create_user() throws Exception {
        CreateUserRequestDto req = CreateUserRequestDto.builder()
                .username("alice")
                .password("password")
                .roles(Set.of("EMPLOYEE"))
                .build();

        UserDto out = UserDto.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .roles(Set.of("EMPLOYEE"))
                .enabled(true)
                .build();
        UUID id = out.getId();

        given(userService.create(any(CreateUserRequestDto.class))).willReturn(out);


        mvc.perform(post("/api/v1/auth/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("EMPLOYEE")))
                .andExpect(jsonPath("$.enabled", is(true)));
    }


    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void set_roles() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateUserRolesRequestDto req = UpdateUserRolesRequestDto.builder()
                .roles(Set.of("EMPLOYEE", "HR"))
                .build();
        UserDto out = UserDto.builder()
                .id(id)
                .roles(Set.of("EMPLOYEE", "HR"))
                .build();
        given(userService.setRoles(eq(id), any(UpdateUserRolesRequestDto.class))).willReturn(out);

        mvc.perform(put("/api/v1/auth/users/{id}/roles", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", containsInAnyOrder("HR", "EMPLOYEE")));
    }


    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void set_managedDepartments() throws Exception {
        UUID id = UUID.randomUUID();
        Set<UUID> body = Set.of(UUID.randomUUID(), UUID.randomUUID());
        UserDto out = UserDto.builder()
                .id(id)
                .managedDeptIds(body)
                .build();
        given(userService.setManagedDepartments(eq(id), anySet())).willReturn(out);

        mvc.perform(put("/api/v1/auth/users/{id}/managed-departments", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managedDeptIds", hasSize(2)));
    }


    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void change_password() throws Exception {
        UUID id = UUID.randomUUID();
        ChangePasswordRequestDto req = ChangePasswordRequestDto.builder()
                .newPassword("newPassword")
                .build();

        mvc.perform(post("/api/v1/auth/users/{id}/password", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void set_enabled() throws Exception {
        UUID id = UUID.randomUUID();
        SetEnabledRequestDto req = SetEnabledRequestDto.builder()
                .enabled(true)
                .build();
        UserDto out = UserDto.builder()
                .id(id)
                .enabled(true)
                .build();
        given(userService.setEnabled(id, true)).willReturn(out);

        mvc.perform(post("/api/v1/auth/users/{id}/enabled", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(true)));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void get_user_ok() throws Exception {
        UUID id = UUID.randomUUID();
        UserDto out = UserDto.builder()
                .id(id).username("alice")
                .roles(Set.of("EMPLOYEE"))
                .enabled(true)
                .build();
        given(userService.get(id)).willReturn(out);

        mvc.perform(get("/api/v1/auth/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void get_user_notFound_404() throws Exception {
        UUID id = UUID.randomUUID();
        given(userService.get(id)).willThrow(new jakarta.persistence.EntityNotFoundException());
        mvc.perform(get("/api/v1/auth/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void findAll_ok_returnsPage() throws Exception {
        var u1 = UserDto.builder().id(UUID.randomUUID()).username("a").roles(Set.of()).enabled(true).build();
        var u2 = UserDto.builder().id(UUID.randomUUID()).username("b").roles(Set.of()).enabled(false).build();
        var page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(u1, u2));
        given(userService.findAll(any(org.springframework.data.domain.Pageable.class))).willReturn(page);

        mvc.perform(get("/api/v1/auth/users")
                        .param("page","0").param("size","2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].username").value("a"))
                .andExpect(jsonPath("$.content[1].username").value("b"));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void delete_ok() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/v1/auth/users/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());
        org.mockito.Mockito.verify(userService).delete(id);
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    void write_endpoints_forbidden_for_nonSupervisor() throws Exception {
        CreateUserRequestDto req = CreateUserRequestDto.builder()
                .username("xxxxxx").password("password").roles(Set.of("EMPLOYEE")).build();

        mvc.perform(post("/api/v1/auth/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void create_user_validation_400() throws Exception {
        CreateUserRequestDto bad = CreateUserRequestDto.builder()
                .username("a")
                .password("p")
                .roles(Set.of("EMPLOYEE"))
                .build();

        mvc.perform(post("/api/v1/auth/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

}