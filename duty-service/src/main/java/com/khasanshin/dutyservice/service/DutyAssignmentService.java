package com.khasanshin.dutyservice.service;

import com.khasanshin.dutyservice.dto.AssignDutyDto;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import com.khasanshin.dutyservice.entity.DepartmentDutyAssignment;
import com.khasanshin.dutyservice.exception.RemoteServiceUnavailableException;
import com.khasanshin.dutyservice.feign.EmployeeClient;
import com.khasanshin.dutyservice.feign.OrgClient;
import com.khasanshin.dutyservice.mapper.DutyAssignmentMapper;
import com.khasanshin.dutyservice.repository.DutyAssignmentRepository;
import com.khasanshin.dutyservice.repository.DutyRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DutyAssignmentService {

  private final DutyAssignmentRepository assignmentRepo;
  private final DutyAssignmentMapper mapper;
  private final DutyRepository dutyRepo;
  private final OrgClient orgClient;
  private final EmployeeClient employeeClient;

  @Transactional
  public DutyAssignmentDto assign(UUID departmentId, AssignDutyDto req) {
    if (assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(
        departmentId, req.getEmployeeId(), req.getDutyId())) {
      throw new IllegalStateException("duty already assigned to employee in this department");
    }

    ensureDepartmentExists(departmentId);
    ensureEmployeeExists(req.getEmployeeId());
    if (req.getAssignedBy() != null) ensureEmployeeExists(req.getAssignedBy());
    ensureDutyExists(req.getDutyId());

    DepartmentDutyAssignment a =
        DepartmentDutyAssignment.builder()
            .departmentId(departmentId)
            .employeeId(req.getEmployeeId())
            .dutyId(req.getDutyId())
            .note(req.getNote())
            .assignedBy(req.getAssignedBy())
            .build();

    a = assignmentRepo.saveAndFlush(a);
    return mapper.toDto(a);
  }

  public Page<DutyAssignmentDto> list(UUID departmentId, Pageable pageable) {
    Pageable sorted =
        pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "assignedAt"));
    return assignmentRepo.findByDepartmentId(departmentId, sorted).map(mapper::toDto);
  }

  @Transactional
  public void unassign(UUID departmentId, UUID assignmentId) {
    var a =
        assignmentRepo
            .findById(assignmentId)
            .orElseThrow(
                () -> new EntityNotFoundException("assignment not found: " + assignmentId));
    if (!a.getDepartmentId().equals(departmentId)) {
      throw new EntityNotFoundException("assignment not in department");
    }
    assignmentRepo.delete(a);
  }

  @CircuitBreaker(name = "orgClient", fallbackMethod = "ensureDepartmentExistsUnavailable")
  void ensureDepartmentExists(UUID id) { orgClient.departmentExists(id); }

  @CircuitBreaker(name = "employeeClient", fallbackMethod = "ensureEmployeeExistsUnavailable")
  void ensureEmployeeExists(UUID id)   { employeeClient.employeeExists(id); }


  void ensureDepartmentExistsUnavailable(UUID id, Throwable cause) {
    throw new RemoteServiceUnavailableException("organization-service unavailable", cause);
  }

  void ensureEmployeeExistsUnavailable(UUID id, Throwable cause) {
    throw new RemoteServiceUnavailableException("employee-service unavailable", cause);
  }


  void ensureDutyExists(UUID id) {
    if (!dutyRepo.existsById(id)) throw new EntityNotFoundException("duty " + id);
  }
}
