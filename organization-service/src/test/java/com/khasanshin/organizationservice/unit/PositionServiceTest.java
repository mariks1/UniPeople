package com.khasanshin.organizationservice.unit;

import com.khasanshin.organizationservice.application.PositionApplicationService;
import com.khasanshin.organizationservice.domain.model.Position;
import com.khasanshin.organizationservice.domain.port.PositionRepositoryPort;
import com.khasanshin.organizationservice.dto.*;
import com.khasanshin.organizationservice.mapper.PositionMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PositionServiceTest {

    @Mock
    PositionRepositoryPort positionRepository;
    @Mock
    PositionMapper mapper;

    PositionApplicationService service;

    @BeforeEach
    void setUp() {
        service = new PositionApplicationService(positionRepository, mapper);
    }

    @Test
    void get_ok() {
        UUID id = UUID.randomUUID();
        Position p = Position.builder().id(id).name("X").build();
        when(positionRepository.findById(id)).thenReturn(Optional.of(p));
        when(mapper.toDto(p)).thenReturn(PositionDto.builder().build());

        assertNotNull(service.get(id));
    }

    @Test
    void get_notFound() {
        when(positionRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()));
    }

    @Test
    void create_ok_thenSaves() {
        var dto = CreatePositionDto.builder()
                .name("Pos")
                .build();

        Position entity = Position.builder().name("Pos").build();
        Position saved = entity.toBuilder().id(UUID.randomUUID()).build();
        when(mapper.toDomain(dto)).thenReturn(entity);
        when(positionRepository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(PositionDto.builder().build());

        assertNotNull(service.create(dto));

        verify(positionRepository).save(entity);
    }

    @Test
    void update_ok_updatesEntity_andValidates() {
        UUID id = UUID.randomUUID();
        var dto = UpdatePositionDto.builder()
                .name("NewName")
                .build();

        Position e = Position.builder().id(id).name("Old").build();
        Position updated = Position.builder().id(id).name("NewName").build();
        when(positionRepository.findById(id)).thenReturn(Optional.of(e));
        when(mapper.updateDomain(dto, e)).thenReturn(updated);
        when(positionRepository.save(updated)).thenReturn(updated);
        when(mapper.toDto(updated)).thenReturn(PositionDto.builder().build());

        assertNotNull(service.update(id, dto));

        verify(mapper, times(1)).updateDomain(dto, e);
    }

    @Test
    void delete_ok() {
        UUID id = UUID.randomUUID();

        when(positionRepository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(positionRepository).deleteById(id);
    }

    @Test
    void findAll_usesDefaultSort_whenUnsortedAndNoQuery() {
        Pageable input = PageRequest.of(0, 10);
        var entities = List.of(make("A"), make("B"));
        when(positionRepository.findAll(any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(0);
                    return new PageImpl<>(entities, p, 2);
                });
        when(mapper.toDto(any()))
                .thenAnswer(inv -> {
                    Position e = inv.getArgument(0);
                    return PositionDto.builder().id(e.getId()).name(e.getName()).build();
                });

        Page<PositionDto> result = service.findAll(null, input);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(positionRepository).findAll(captor.capture());
        Pageable passed = captor.getValue();

        assertEquals(0, passed.getPageNumber());
        assertEquals(10, passed.getPageSize());
        assertTrue(passed.getSort().isSorted());
        assertEquals(Sort.Direction.ASC, passed.getSort().getOrderFor("name").getDirection());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    private Position make(String name) {
        return Position.builder()
                .id(UUID.randomUUID())
                .name(name)
                .build();
    }

    @Test
    void findAll_keepsValidSort_whenProvided() {
        Pageable input = PageRequest.of(1, 5, Sort.by(Sort.Order.desc("name")));
        var entities = List.of(make("X"));
        when(positionRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities, input, 6));
        when(mapper.toDto(any()))
                .thenAnswer(inv -> {
                    Position e = inv.getArgument(0);
                    return PositionDto.builder().id(e.getId()).name(e.getName()).build();
                });

        Page<PositionDto> result = service.findAll("", input);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(positionRepository).findAll(captor.capture());
        Pageable passed = captor.getValue();

        assertEquals(1, passed.getPageNumber());
        assertEquals(5, passed.getPageSize());
        assertEquals(Sort.by(Sort.Order.desc("name")), passed.getSort());
        assertEquals(6, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void findAll_filtersOutUnknownSortFields_andFallsBackToDefault() {
        Pageable input = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("unknown")));
        var entities = List.of(make("A"));
        when(positionRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities, input, 1));
        when(mapper.toDto(any()))
                .thenAnswer(inv -> {
                    Position e = inv.getArgument(0);
                    return PositionDto.builder().id(e.getId()).name(e.getName()).build();
                });

        service.findAll(null, input);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(positionRepository).findAll(captor.capture());
        Pageable passed = captor.getValue();

        assertTrue(passed.getSort().isSorted());
        assertNotNull(passed.getSort().getOrderFor("name"));
        assertEquals(Sort.Direction.ASC, passed.getSort().getOrderFor("name").getDirection());
        assertNull(passed.getSort().getOrderFor("unknown"));
    }

    @Test
    void findAll_searchesByName_whenQueryProvided_trimmed() {
        Pageable input = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name")));
        var entities = List.of(make("Manager"));
        when(positionRepository.findByNameContainingIgnoreCase(eq("man"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities, input, 1));
        when(mapper.toDto(any()))
                .thenAnswer(inv -> {
                    Position e = inv.getArgument(0);
                    return PositionDto.builder().id(e.getId()).name(e.getName()).build();
                });

        Page<PositionDto> result = service.findAll("  man  ", input);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(positionRepository).findByNameContainingIgnoreCase(eq("man"), captor.capture());
        Pageable passed = captor.getValue();

        assertEquals(0, passed.getPageNumber());
        assertEquals(10, passed.getPageSize());
        assertEquals(Sort.by(Sort.Order.asc("name")), passed.getSort());
        assertEquals(1, result.getTotalElements());
        assertEquals("Manager", result.getContent().get(0).getName());
    }

    @Test
    void exists_ok() {
        UUID id = UUID.randomUUID();

        when(positionRepository.existsById(id)).thenReturn(true);

        boolean result = service.exists(id);

        assertTrue(result);
        verify(positionRepository).existsById(id);
    }


}
