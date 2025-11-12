package com.khasanshin.authservice;

import com.khasanshin.authservice.dto.ChangePasswordRequestDto;
import com.khasanshin.authservice.dto.CreateUserRequestDto;
import com.khasanshin.authservice.dto.UpdateUserRolesRequestDto;
import com.khasanshin.authservice.dto.UserDto;
import com.khasanshin.authservice.entity.AppUser;
import com.khasanshin.authservice.mapper.UserMapper;
import com.khasanshin.authservice.repository.UserRepository;
import com.khasanshin.authservice.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repo;
    @Mock UserMapper mapper;
    @Mock PasswordEncoder passwordEncoder;

    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repo, mapper, passwordEncoder);
    }

    private AppUser baseUser(UUID id) {
        return AppUser.builder()
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

        AppUser draft = AppUser.builder().username("whatever").enabled(false).roles(Set.of()).build();
        when(mapper.toEntity(dto)).thenReturn(draft);
        when(passwordEncoder.encode("plain")).thenReturn("ENCODED");

        AppUser saved = baseUser(UUID.randomUUID()).toBuilder()
                .username("alice")
                .roles(Set.of("EMPLOYEE", "HR"))
                .build();
        when(repo.save(any(AppUser.class))).thenReturn(saved);

        UserDto expectedDto = mock(UserDto.class);
        when(mapper.toDto(saved)).thenReturn(expectedDto);

        UserDto result = service.create(dto);

        assertThat(result).isSameAs(expectedDto);

        ArgumentCaptor<AppUser> entityCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(repo).save(entityCaptor.capture());
        AppUser toSave = entityCaptor.getValue();
        assertThat(toSave.getUsername()).isEqualTo("alice");
        assertThat(toSave.getRoles()).containsExactlyInAnyOrder("EMPLOYEE", "HR");
        verify(passwordEncoder).encode("plain");
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
        AppUser entity = baseUser(id);
        UserDto dto = mock(UserDto.class);

        when(repo.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.get(id)).isSameAs(dto);

        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(id)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_mapsEntitiesToDtos() {
        AppUser u1 = baseUser(UUID.randomUUID());
        AppUser u2 = baseUser(UUID.randomUUID());
        UserDto d1 = mock(UserDto.class);
        UserDto d2 = mock(UserDto.class);
        when(repo.findAll(PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(u1, u2)));
        when(mapper.toDto(u1)).thenReturn(d1);
        when(mapper.toDto(u2)).thenReturn(d2);

        Page<UserDto> page = service.findAll(PageRequest.of(0, 2));
        assertThat(page.getContent()).containsExactly(d1, d2);
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
        AppUser entity = baseUser(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        UpdateUserRolesRequestDto dto = mock(UpdateUserRolesRequestDto.class);
        when(dto.getRoles()).thenReturn(Set.of("hr", " employee "));

        AppUser saved = entity.toBuilder().roles(Set.of("HR", "EMPLOYEE")).build();
        when(repo.save(any(AppUser.class))).thenReturn(saved);
        UserDto out = mock(UserDto.class);
        when(mapper.toDto(saved)).thenReturn(out);

        assertThat(service.setRoles(id, dto)).isSameAs(out);

        when(dto.getRoles()).thenReturn(Set.of("GODMODE"));
        assertThatThrownBy(() -> service.setRoles(id, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setManagedDepartments_nullMeansEmpty() {
        UUID id = UUID.randomUUID();
        AppUser entity = baseUser(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        AppUser saved = entity.toBuilder().managedDeptIds(Set.of()).build();
        when(repo.save(any(AppUser.class))).thenReturn(saved);
        UserDto dto = mock(UserDto.class);
        when(mapper.toDto(saved)).thenReturn(dto);

        assertThat(service.setManagedDepartments(id, null)).isSameAs(dto);
        verify(repo).save(argThat(u -> u.getManagedDeptIds().isEmpty()));
    }

    @Test
    void changePassword_encodesAndSaves() {
        UUID id = UUID.randomUUID();
        AppUser entity = baseUser(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));
        ChangePasswordRequestDto dto = mock(ChangePasswordRequestDto.class);
        when(dto.getNewPassword()).thenReturn("new");
        when(passwordEncoder.encode("new")).thenReturn("ENC");

        service.changePassword(id, dto);
        verify(passwordEncoder).encode("new");
        verify(repo).save(entity);
    }

    @Test
    void setEnabled_updatesFlag() {
        UUID id = UUID.randomUUID();
        AppUser entity = baseUser(id);
        when(repo.findById(id)).thenReturn(Optional.of(entity));

        AppUser saved = entity.toBuilder().enabled(false).build();
        when(repo.save(any(AppUser.class))).thenReturn(saved);
        UserDto dto = mock(UserDto.class);
        when(mapper.toDto(saved)).thenReturn(dto);

        assertThat(service.setEnabled(id, false)).isSameAs(dto);
        verify(repo).save(argThat(u -> !u.isEnabled()));
    }

    @Test
    void findByUsername_normalizesAndThrowsWhenMissing() {
        AppUser entity = baseUser(UUID.randomUUID());
        when(repo.findByUsernameIgnoreCase("john.doe")).thenReturn(Optional.of(entity));
        assertThat(service.findByUsername("  John.Doe  ")).isSameAs(entity);

        when(repo.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByUsername("missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}