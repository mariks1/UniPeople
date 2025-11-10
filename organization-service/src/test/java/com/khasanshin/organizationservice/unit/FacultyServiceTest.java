package com.khasanshin.organizationservice.unit;

import com.khasanshin.organizationservice.dto.*;
import com.khasanshin.organizationservice.entity.Faculty;
import com.khasanshin.organizationservice.mapper.FacultyMapper;
import com.khasanshin.organizationservice.repository.FacultyRepository;
import com.khasanshin.organizationservice.service.FacultyService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FacultyServiceTest {


    @Mock
    FacultyRepository facultyRepository;
    @Mock
    FacultyMapper mapper;

    FacultyService service;

    @BeforeEach
    void setUp() {
        service = new FacultyService(facultyRepository, mapper);
    }

    @Test
    void get_ok() {
        UUID id = UUID.randomUUID();
        Faculty f = new Faculty();
        when(facultyRepository.findById(id)).thenReturn(Optional.of(f));
        when(mapper.toDto(f)).thenReturn(FacultyDto.builder().build());

        assertNotNull(service.get(id));
    }

    @Test
    void get_notFound() {
        when(facultyRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()));
    }

    @Test
    void create_ok_thenSaves() {
        var dto = CreateFacultyDto.builder()
                .name("Fac").code("D1")
                .build();

        Faculty entity = new Faculty();
        Faculty saved = new Faculty();
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(facultyRepository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(FacultyDto.builder().build());

        assertNotNull(service.create(dto));

        verify(facultyRepository).save(entity);
    }

    @Test
    void update_ok_updatesEntity_andValidates() {
        UUID id = UUID.randomUUID();
        var dto = UpdateFacultyDto.builder()
                .name("NewName").code("rand")
                .build();

        Faculty e = new Faculty();
        when(facultyRepository.findById(id)).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(FacultyDto.builder().build());

        assertNotNull(service.update(id, dto));

        verify(mapper, times(1)).updateEntity(dto, e);
    }

    @Test
    void delete_ok() {
        UUID id = UUID.randomUUID();
        Faculty dep = spy(new Faculty());
        when(facultyRepository.findById(id)).thenReturn(Optional.of(dep));

        service.delete(id);

        verify(facultyRepository).delete(dep);
    }


    @Test
    void page_usesExistingSort_whenProvided() {
        Pageable input = PageRequest.of(2, 20, Sort.by(Sort.Order.desc("name")));
        var entities = List.of(
                Faculty.builder().id(UUID.randomUUID()).code("C1").name("A").build(),
                Faculty.builder().id(UUID.randomUUID()).code("C2").name("B").build()
        );
        Page<Faculty> page = new PageImpl<>(entities, input, 42);

        when(facultyRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(mapper.toDto(any(Faculty.class)))
                .thenAnswer(inv -> {
                    Faculty f = inv.getArgument(0);
                    return FacultyDto.builder().id(f.getId()).code(f.getCode()).name(f.getName()).build();
                });

        Page<FacultyDto> result = service.page(input);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(facultyRepository).findAll(captor.capture());
        Pageable passed = captor.getValue();

        assertEquals(2, passed.getPageNumber());
        assertEquals(20, passed.getPageSize());
        assertEquals(Sort.by(Sort.Order.desc("name")), passed.getSort());

        assertEquals(2, result.getContent().size());
        assertEquals(42, result.getTotalElements());
    }

    @Test
    void page_addsDefaultSort_whenUnsorted() {
        Pageable input = PageRequest.of(0, 10);
        var entities = List.of(
                Faculty.builder().id(UUID.randomUUID()).code("AA").name("X").build()
        );
        when(facultyRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities, input, 1));
        when(mapper.toDto(any(Faculty.class)))
                .thenAnswer(inv -> {
                    Faculty f = inv.getArgument(0);
                    return FacultyDto.builder().id(f.getId()).code(f.getCode()).name(f.getName()).build();
                });

        service.page(input);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(facultyRepository).findAll(captor.capture());
        Pageable passed = captor.getValue();

        assertEquals(0, passed.getPageNumber());
        assertEquals(10, passed.getPageSize());

        Sort sort = passed.getSort();
        assertTrue(sort.isSorted());
        assertEquals(2, sort.stream().count());
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sort.getOrderFor("code")).getDirection());
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sort.getOrderFor("name")).getDirection());
    }
}
