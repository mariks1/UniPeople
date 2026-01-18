package com.khasanshin.authservice;

import com.khasanshin.authservice.application.UserApplicationService;
import com.khasanshin.authservice.domain.model.User;
import com.khasanshin.authservice.domain.port.PasswordHasherPort;
import com.khasanshin.authservice.domain.port.UserRepositoryPort;
import com.khasanshin.authservice.dto.ChangePasswordRequestDto;
import com.khasanshin.authservice.dto.CreateUserRequestDto;
import com.khasanshin.authservice.dto.UpdateUserRolesRequestDto;
import com.khasanshin.authservice.dto.UserDto;
import com.khasanshin.authservice.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepositoryPort repo;
    @Mock PasswordHasherPort hasher;

    UserMapper mapper;
    UserApplicationService service;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
        service = new UserApplicationService(repo, mapper, hasher);
    }

    private User baseUser(UUID id) {
        return User.builder()
                .id(id)
                .username("john")
                .roles(Set.of("EMPLOYEE"))
                .enabled(true)
                .managedDeptIds(Set.of())
                .build();
    }

    @Test
    void create_normalizesUsername_validatesRoles_encodesPassword_andSetsEnabled() {
        CreateUserRequestDto dto = mock(CreateUserRequestDto.class);
        when(dto.getUsername()).thenReturn("  Alice  ");
        when(dto.getPassword()).thenReturn("plain");
        when(dto.getRoles()).thenReturn(new HashSet<>(Arrays.asList("employee", " Hr ")));
        when(dto.getEnabled()).thenReturn(Boolean.TRUE);

        when(repo.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(hasher.hash("plain")).thenReturn("ENCODED");

        User saved = User.builder()
                .username("alice")
                .passwordHash("ENCODED")
                .id(UUID.randomUUID())
                .roles(Set.of("EMPLOYEE", "HR"))
                .enabled(true)
                .build();
        ArgumentCaptor<User> toSaveCaptor = ArgumentCaptor.forClass(User.class);
        when(repo.save(any(User.class))).thenReturn(saved);

        UserDto result = service.create(dto);

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getRoles()).containsExactlyInAnyOrder("EMPLOYEE", "HR");
        verify(hasher).hash("plain");
        verify(repo).save(toSaveCaptor.capture());
        assertThat(toSaveCaptor.getValue().getUsername()).isEqualTo("alice");
    }

    @Test
    void create_throws_whenUsernameExists() {
        CreateUserRequestDto dto = mock(CreateUserRequestDto.class);
        when(dto.getUsername()).thenReturn("Bob");
        when(repo.existsByUsernameIgnoreCase("bob")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("bob");
        verify(repo, never()).save(any());
    }

    @Test
    void create_throws_onUnknownRole() {
        CreateUserRequestDto dto = mock(CreateUserRequestDto.class);
        when(dto.getUsername()).thenReturn("user");
        when(repo.existsByUsernameIgnoreCase("user")).thenReturn(false);
        when(dto.getRoles()).thenReturn(Set.of("EMPLOYEE", "HACKER"));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown roles");
    }

    @Test
    void get_returnsDto_orThrows() {
        UUID id = UUID.randomUUID();
        User entity = baseUser(id);

        when(repo.findById(id)).thenReturn(Optional.of(entity));

        assertThat(service.get(id).getId()).isEqualTo(id);

        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(id)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_mapsEntitiesToDtos() {
        User u1 = baseUser(UUID.randomUUID());
        User u2 = baseUser(UUID.randomUUID());
        when(repo.findAll(PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(u1, u2)));

        Page<UserDto> page = service.findAll(PageRequest.of(0, 2));
        assertThat(page.getContent())
                .extracting(UserDto::getId)
                .containsExactly(u1.getId(), u2.getId());
    }

    @Test
    void delete_checksExistence() {
        UUID id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(EntityNotFoundException.class);

        when(repo.existsById(id)).thenReturn(true);
        service.delete(id);
        verify(repo).deleteById(id);
    }

    @Test
    void setRoles_validatesAndSaves() {
        UUID id = UUID.randomUUID();
        User entity = baseUser(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        UpdateUserRolesRequestDto dto = mock(UpdateUserRolesRequestDto.class);
        when(dto.getRoles()).thenReturn(Set.of("hr", " employee "));

        User saved = entity.toBuilder().roles(Set.of("HR", "EMPLOYEE")).build();
        when(repo.save(any(User.class))).thenReturn(saved);

        UserDto out = service.setRoles(id, dto);
        assertThat(out.getRoles()).containsExactlyInAnyOrder("HR", "EMPLOYEE");

        when(dto.getRoles()).thenReturn(Set.of("GODMODE"));
        assertThatThrownBy(() -> service.setRoles(id, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setManagedDepartments_nullMeansEmpty() {
        UUID id = UUID.randomUUID();
        User entity = baseUser(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        User saved = entity.toBuilder().managedDeptIds(Set.of()).build();
        when(repo.save(any(User.class))).thenReturn(saved);

        assertThat(service.setManagedDepartments(id, null).getManagedDeptIds()).isEmpty();
        verify(repo).save(argThat(u -> u.getManagedDeptIds().isEmpty()));
    }

    @Test
    void changePassword_encodesAndSaves() {
        UUID id = UUID.randomUUID();
        User entity = baseUser(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));
        ChangePasswordRequestDto dto = mock(ChangePasswordRequestDto.class);
        when(dto.getNewPassword()).thenReturn("new");
        when(hasher.hash("new")).thenReturn("ENC");

        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.changePassword(id, dto);
        verify(hasher).hash("new");
        verify(repo).save(argThat(u -> "ENC".equals(u.getPasswordHash())));
    }

    @Test
    void setEnabled_updatesFlag() {
        UUID id = UUID.randomUUID();
        User entity = baseUser(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        User saved = entity.toBuilder().enabled(false).build();
        when(repo.save(any(User.class))).thenReturn(saved);

        UserDto dto = service.setEnabled(id, false);
        assertThat(dto.isEnabled()).isFalse();
        verify(repo).save(argThat(u -> !u.isEnabled()));
    }
}
