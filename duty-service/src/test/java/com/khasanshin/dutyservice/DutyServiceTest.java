package com.khasanshin.dutyservice;

import com.khasanshin.dutyservice.dto.CreateDutyDto;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import com.khasanshin.dutyservice.dto.DutyDto;
import com.khasanshin.dutyservice.dto.UpdateDutyDto;
import com.khasanshin.dutyservice.entity.DepartmentDutyAssignment;
import com.khasanshin.dutyservice.entity.Duty;
import com.khasanshin.dutyservice.mapper.DutyAssignmentMapper;
import com.khasanshin.dutyservice.mapper.DutyMapper;
import com.khasanshin.dutyservice.repository.DutyAssignmentRepository;
import com.khasanshin.dutyservice.repository.DutyRepository;
import com.khasanshin.dutyservice.service.DutyService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DutyServiceTest {

    @Mock DutyRepository dutyRepository;
    @Mock DutyAssignmentRepository dutyAssignmentRepository;
    @Mock DutyMapper dutyMapper;
    @Mock DutyAssignmentMapper dutyAssignmentMapper;

    DutyService service;

    @BeforeEach
    void setUp() {
        service = new DutyService(dutyRepository, dutyAssignmentRepository, dutyMapper, dutyAssignmentMapper);
    }

    @Test
    void findAll_appliesDefaultSort_whenUnsorted() {
        Pageable in = PageRequest.of(0, 10);
        when(dutyRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(in);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(dutyRepository).findAll(captor.capture());
        Sort sort = captor.getValue().getSort();
        assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sort.getOrderFor("code")).getDirection());
        assertEquals(1, sort.stream().count());
    }

    @Test
    void findAll_keepsIncomingSort_whenProvided() {
        Pageable in = PageRequest.of(1, 5, Sort.by(Sort.Order.desc("name")));
        when(dutyRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(in);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(dutyRepository).findAll(captor.capture());
        assertEquals(Sort.by(Sort.Order.desc("name")), captor.getValue().getSort());
    }

    @Test
    void get_ok() {
        UUID id = UUID.randomUUID();
        Duty e = new Duty();
        when(dutyRepository.findById(id)).thenReturn(Optional.of(e));
        when(dutyMapper.toDto(e)).thenReturn(DutyDto.builder().build());

        assertNotNull(service.get(id));
    }

    @Test
    void get_notFound() {
        when(dutyRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()));
    }

    @Test
    void create_ok_savesAndReturns() {
        CreateDutyDto dto = CreateDutyDto.builder().code("X").name("Name").build();
        when(dutyRepository.existsByCodeIgnoreCase("X")).thenReturn(false);

        Duty toSave = new Duty();
        Duty saved = new Duty();
        when(dutyMapper.toEntity(dto)).thenReturn(toSave);
        when(dutyRepository.saveAndFlush(toSave)).thenReturn(saved);
        when(dutyMapper.toDto(saved)).thenReturn(DutyDto.builder().build());

        assertNotNull(service.create(dto));
        verify(dutyRepository).saveAndFlush(toSave);
    }

    @Test
    void create_rejectsDuplicateCode_precheck() {
        CreateDutyDto dto = CreateDutyDto.builder().code("X").build();
        when(dutyRepository.existsByCodeIgnoreCase("X")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.create(dto));
        verify(dutyRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_rejectsDuplicateCode_onConstraint() {
        CreateDutyDto dto = CreateDutyDto.builder().code("X").build();
        when(dutyRepository.existsByCodeIgnoreCase("X")).thenReturn(false);

        Duty toSave = new Duty();
        when(dutyMapper.toEntity(dto)).thenReturn(toSave);
        when(dutyRepository.saveAndFlush(toSave)).thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(IllegalStateException.class, () -> service.create(dto));
    }

    @Test
    void update_ok_flushesAndMaps() {
        UUID id = UUID.randomUUID();
        UpdateDutyDto dto = UpdateDutyDto.builder().name("N").build();
        Duty e = new Duty();

        when(dutyRepository.findById(id)).thenReturn(Optional.of(e));
        doAnswer(inv -> null).when(dutyMapper).updateEntity(dto, e);
        when(dutyMapper.toDto(e)).thenReturn(DutyDto.builder().build());

        assertNotNull(service.update(id, dto));
        verify(dutyRepository).flush();
    }

    @Test
    void update_404_whenNotFound() {
        when(dutyRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.update(UUID.randomUUID(), UpdateDutyDto.builder().build()));
    }

    @Test
    void update_duplicateCode_onFlush() {
        UUID id = UUID.randomUUID();
        UpdateDutyDto dto = UpdateDutyDto.builder().code("X").build();
        Duty e = new Duty();
        when(dutyRepository.findById(id)).thenReturn(Optional.of(e));
        doAnswer(inv -> null).when(dutyMapper).updateEntity(dto, e);
        doThrow(new DataIntegrityViolationException("dup")).when(dutyRepository).flush();

        assertThrows(IllegalStateException.class, () -> service.update(id, dto));
    }

    @Test
    void delete_ok() {
        UUID id = UUID.randomUUID();
        when(dutyRepository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(dutyRepository).deleteById(id);
    }

    @Test
    void delete_404_whenNotExists() {
        when(dutyRepository.existsById(any())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.delete(UUID.randomUUID()));
        verify(dutyRepository, never()).deleteById(any());
    }

    @Test
    void listAssignments_defaultSort_whenUnsorted() {
        UUID dutyId = UUID.randomUUID();
        Pageable in = PageRequest.of(0, 10);
        when(dutyAssignmentRepository.findByDutyId(any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.listAssignments(dutyId, in);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(dutyAssignmentRepository).findByDutyId(eq(dutyId), captor.capture());
        Sort sort = captor.getValue().getSort();
        assertEquals(Sort.Direction.DESC, Objects.requireNonNull(sort.getOrderFor("assignedAt")).getDirection());
    }

    @Test
    void listAssignments_mapsItems() {
        UUID dutyId = UUID.randomUUID();
        Pageable in = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("assignedAt")));
        DepartmentDutyAssignment a = new DepartmentDutyAssignment();

        when(dutyAssignmentRepository.findByDutyId(dutyId, in))
                .thenReturn(new PageImpl<>(List.of(a), in, 1));
        when(dutyAssignmentMapper.toDto(a)).thenReturn(DutyAssignmentDto.builder().build());

        Page<DutyAssignmentDto> page = service.listAssignments(dutyId, in);

        assertEquals(1, page.getTotalElements());
        assertEquals(1, page.getContent().size());
    }
}
