package com.khasanshin.employeeservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.khasanshin.employeeservice.dto.CreateEmployeeDto;
import com.khasanshin.employeeservice.dto.EmployeeDto;
import com.khasanshin.employeeservice.dto.UpdateEmployeeDto;
import com.khasanshin.employeeservice.entity.Employee;
import com.khasanshin.employeeservice.exception.RemoteServiceUnavailableException;
import com.khasanshin.employeeservice.feign.OrgClient;
import com.khasanshin.employeeservice.mapper.EmployeeMapper;
import com.khasanshin.employeeservice.repository.EmployeeRepository;
import com.khasanshin.employeeservice.service.EmployeeService;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    EmployeeMapper mapper;

    @Mock
    OrgClient orgClient;

    EmployeeService service;

    private Employee employee(UUID id, String firstName, String lastName,
                              Employee.Status status, Instant createdAt, UUID dept) {
        Employee e = new Employee();
        e.setId(id);
        e.setFirstName(firstName);
        e.setLastName(lastName);
        e.setStatus(status);
        e.setCreatedAt(createdAt);
        e.setDepartment(dept);
        return e;
    }

    private EmployeeDto dtoFromEntity(Employee e) {
        return EmployeeDto.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .middleName(e.getMiddleName())
                .workEmail(e.getWorkEmail())
                .phone(e.getPhone())
                .departmentId(e.getDepartment())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    @BeforeEach
    void setUp() {
        service = new EmployeeService(employeeRepository, mapper, orgClient);
    }

    @Test
    void exists_ok() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.existsById(id)).thenReturn(true);

        assertTrue(service.exists(id));
        verify(employeeRepository).existsById(id);
    }

    @Test
    void get_ok() {
        UUID id = UUID.randomUUID();
        Employee d = new Employee();
        when(employeeRepository.findById(id)).thenReturn(Optional.of(d));
        when(mapper.toDto(d)).thenReturn(EmployeeDto.builder().build());

        assertNotNull(service.get(id));
    }

    @Test
    void get_notFound() {
        when(employeeRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()));
    }

    @Test
    void create_ok_validatesDepartment_thenSaves() {
        UUID dep = UUID.randomUUID();
        var dto = CreateEmployeeDto.builder()
                .firstName("A").lastName("B")
                .departmentId(dep)
                .build();

        Employee toSave = new Employee();
        when(mapper.toEntity(dto)).thenReturn(toSave);
        doNothing().when(orgClient).departmentExists(dep);

        Employee saved = employee(UUID.randomUUID(), "A", "B",
                Employee.Status.ACTIVE, Instant.now(), dep);
        when(employeeRepository.save(toSave)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(dtoFromEntity(saved));

        EmployeeDto result = service.create(dto);

        assertNotNull(result);
        assertEquals(dep, result.getDepartmentId());
        verify(orgClient).departmentExists(dep);
        verify(employeeRepository).save(toSave);
    }

    @Test
    void create_departmentNotFound_mapsTo404() {
        UUID dep = UUID.randomUUID();
        CreateEmployeeDto dto = CreateEmployeeDto.builder().departmentId(dep).build();

        when(mapper.toEntity(dto)).thenReturn(new Employee());
        doThrow(mock(FeignException.NotFound.class)).when(orgClient).departmentExists(dep);

        assertThrows(EntityNotFoundException.class, () -> service.create(dto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void update_ok_updatesFields_andDepartment() {
        UUID id = UUID.randomUUID();
        UUID dep = UUID.randomUUID();

        UpdateEmployeeDto dto = UpdateEmployeeDto.builder()
                .firstName("New").lastName("Name").middleName("M").departmentId(dep).build();

        Employee e = employee(id, "Old", "X", Employee.Status.ACTIVE, Instant.now(), null);
        when(employeeRepository.findById(id)).thenReturn(Optional.of(e));
        doNothing().when(orgClient).departmentExists(dep);

        EmployeeDto mapped = dtoFromEntity(e);
        when(mapper.toDto(e)).thenReturn(mapped);

        EmployeeDto result = service.update(id, dto);

        assertEquals("New", e.getFirstName());
        assertEquals("Name", e.getLastName());
        assertEquals("M", e.getMiddleName());
        assertEquals(dep, e.getDepartment());
        assertSame(mapped, result);
        verify(orgClient).departmentExists(dep);
    }

    @Test
    void update_notFound_throws404() {
        when(employeeRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.update(UUID.randomUUID(),
                UpdateEmployeeDto.builder().build()));
    }

    @Test
    void update_departmentNotFound_mapsTo404() {
        UUID id = UUID.randomUUID();
        UUID dep = UUID.randomUUID();
        UpdateEmployeeDto dto = UpdateEmployeeDto.builder().departmentId(dep).build();

        Employee e = employee(id, "A", "B", Employee.Status.ACTIVE, Instant.now(), null);
        when(employeeRepository.findById(id)).thenReturn(Optional.of(e));
        doThrow(mock(FeignException.NotFound.class)).when(orgClient).departmentExists(dep);

        assertThrows(EntityNotFoundException.class, () -> service.update(id, dto));
    }

    @Test
    void fire_ok_transitionsToFired_clearsHead_andNullsDepartment() {
        UUID id = UUID.randomUUID();
        UUID dep = UUID.randomUUID();

        Employee e = employee(id, "A", "B", Employee.Status.ACTIVE, Instant.now(), dep);
        when(employeeRepository.findById(id)).thenReturn(Optional.of(e));
        doNothing().when(orgClient).clearHeadByEmployee(id);

        EmployeeDto mapped = dtoFromEntity(e);
        when(mapper.toDto(e)).thenReturn(mapped);

        EmployeeDto result = service.fire(id);

        assertEquals(Employee.Status.FIRED, e.getStatus());
        assertNull(e.getDepartment());
        assertSame(mapped, result);
        verify(orgClient).clearHeadByEmployee(id);
    }

    @Test
    void fire_whenAlreadyFired_skipsClearHeadAndReturns() {
        UUID id = UUID.randomUUID();
        Employee e = employee(id, "A", "B", Employee.Status.FIRED, Instant.now(), UUID.randomUUID());
        when(employeeRepository.findById(id)).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        spy(service);
        EmployeeService spyService;

        spyService = spy(new EmployeeService(employeeRepository, mapper, orgClient));

        EmployeeDto out = spyService.fire(id);

        assertNotNull(out);
        verify(spyService, never()).clearHeadByEmployee(any());
        verify(orgClient, never()).clearHeadByEmployee(any());
    }

    @Test
    void fire_notFound_throws404() {
        when(employeeRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.fire(UUID.randomUUID()));
    }

    @Test
    void delete_ok_deletesById() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(employeeRepository).deleteById(id);
    }

    @Test
    void delete_notFound_throws404() {
        when(employeeRepository.existsById(any())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.delete(UUID.randomUUID()));
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    void delete_withReferences_mapsToIllegalState() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk"))
                .when(employeeRepository).deleteById(id);

        assertThrows(IllegalStateException.class, () -> service.delete(id));
    }

    @Test
    void findAll_appliesDefaultSort_whenUnsorted() {
        Pageable in = PageRequest.of(0, 10);
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(in);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findAll(captor.capture());
        Sort sort = captor.getValue().getSort();

        assertEquals(Sort.Direction.DESC, Objects.requireNonNull(sort.getOrderFor("createdAt")).getDirection());
        assertEquals(Sort.Direction.DESC, Objects.requireNonNull(sort.getOrderFor("id")).getDirection());
        assertEquals(2, sort.stream().count());
    }

    @Test
    void findAll_keepsIncomingSort_whenProvided() {
        Pageable in = PageRequest.of(1, 5, Sort.by(Sort.Order.asc("lastName")));
        when(employeeRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), in, 0));

        service.findAll(in);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findAll(captor.capture());
        assertEquals(Sort.by(Sort.Order.asc("lastName")), captor.getValue().getSort());
    }

    @Test
    void stream_noCursor_hasNextTrue_setsNextCursor() {
        Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2024-01-01T09:00:00Z");
        Employee e1 = employee(UUID.randomUUID(), "A", "B", Employee.Status.ACTIVE, t1, null);
        Employee e2 = employee(UUID.randomUUID(), "C", "D", Employee.Status.ACTIVE, t2, null);

        when(mapper.toDto(any(Employee.class))).thenAnswer(inv -> dtoFromEntity(inv.getArgument(0)));

        Slice<Employee> slice = new SliceImpl<>(List.of(e1, e2),
                PageRequest.of(0, 2, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
                true);

        when(employeeRepository.findAllBy(any(Pageable.class))).thenReturn(slice);

        Map<String, Object> res = service.stream(null, 2);

        @SuppressWarnings("unchecked")
        List<EmployeeDto> items = (List<EmployeeDto>) res.get("items");

        assertEquals(2, items.size());
        assertTrue((Boolean) res.get("hasNext"));
        assertEquals(t2, res.get("nextCursor"));
    }

    @Test
    void stream_withCursor_noHasNext_nullNextCursor() {
        Instant cursor = Instant.parse("2024-01-02T00:00:00Z");
        Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
        Employee e1 = employee(UUID.randomUUID(), "A", "B", Employee.Status.ACTIVE, t1, null);

        when(mapper.toDto(any(Employee.class))).thenAnswer(inv -> dtoFromEntity(inv.getArgument(0)));

        Slice<Employee> slice = new SliceImpl<>(List.of(e1),
                PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))),
                false);

        when(employeeRepository.findByCreatedAtLessThan(eq(cursor), any(Pageable.class)))
                .thenReturn(slice);

        Map<String, Object> res = service.stream(cursor, 1);

        assertEquals(false, res.get("hasNext"));
        assertNull(res.get("nextCursor"));
    }

    @Test
    void stream_limitsSizeBetween1And50() {

        when(employeeRepository.findAllBy(any(Pageable.class)))
                .thenAnswer(inv -> new SliceImpl<Employee>(
                        List.of(),
                        inv.getArgument(0),
                        false
                ));
        service.stream(null, -10);
        service.stream(null, 500);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository, times(2)).findAllBy(captor.capture());

        List<Pageable> calls = captor.getAllValues();
        assertEquals(1,  calls.get(0).getPageSize());
        assertEquals(50, calls.get(1).getPageSize());
    }

    @Test
    void activate_ok_setsActive() {
        UUID id = UUID.randomUUID();
        Employee e = employee(id, "A", "B", Employee.Status.FIRED, Instant.now(), null);
        when(employeeRepository.findById(id)).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        EmployeeDto out = service.activate(id);

        assertEquals(Employee.Status.ACTIVE, e.getStatus());
        assertNotNull(out);
    }

    @Test
    void ensureDepartmentUnavailable_mapsTo503() {
        assertThrows(RemoteServiceUnavailableException.class,
                () -> service.ensureDepartmentUnavailable(UUID.randomUUID(), new RuntimeException("x")));
    }

    @Test
    void ignoreClearHead_doesNothing() {
        assertDoesNotThrow(() -> service.ignoreClearHead(UUID.randomUUID(), new RuntimeException("x")));
    }

}
