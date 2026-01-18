package com.khasanshin.dutyservice.application;

import com.khasanshin.dutyservice.domain.model.DutyAssignment;
import com.khasanshin.dutyservice.domain.port.DutyAssignmentRepositoryPort;
import com.khasanshin.dutyservice.domain.port.DutyRepositoryPort;
import com.khasanshin.dutyservice.domain.port.EmployeeVerifierPort;
import com.khasanshin.dutyservice.domain.port.OrgVerifierPort;
import com.khasanshin.dutyservice.dto.AssignDutyDto;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import com.khasanshin.dutyservice.mapper.DutyAssignmentMapper;
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
public class DutyAssignmentApplicationService implements DutyAssignmentUseCase {

    private final DutyAssignmentRepositoryPort assignmentRepo;
    private final DutyAssignmentMapper mapper;
    private final DutyRepositoryPort dutyRepo;
    private final EmployeeVerifierPort employeeVerifier;
    private final OrgVerifierPort orgVerifier;

    @Override
    @Transactional
    public DutyAssignmentDto assign(UUID departmentId, AssignDutyDto req) {
        if (assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(
                departmentId, req.getEmployeeId(), req.getDutyId())) {
            throw new IllegalStateException("duty already assigned to employee in this department");
        }

        orgVerifier.ensureDepartmentExists(departmentId);
        employeeVerifier.ensureEmployeeExists(req.getEmployeeId());
        if (req.getAssignedBy() != null) employeeVerifier.ensureEmployeeExists(req.getAssignedBy());
        ensureDutyExists(req.getDutyId());

        DutyAssignment a =
                DutyAssignment.builder()
                        .departmentId(departmentId)
                        .employeeId(req.getEmployeeId())
                        .dutyId(req.getDutyId())
                        .note(req.getNote())
                        .assignedBy(req.getAssignedBy())
                        .build();

        a = assignmentRepo.save(a);
        return mapper.toDto(a);
    }

    @Override
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

    @Override
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

    @Override
    @Transactional
    public DutyAssignmentDto unassignAndReturn(UUID departmentId, UUID assignmentId) {
        DutyAssignment a = assignmentRepo.findByIdAndDepartmentId(assignmentId, departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found in department"));

        DutyAssignmentDto dto = mapper.toDto(a);

        assignmentRepo.delete(a);

        return dto;
    }

    void ensureDutyExists(UUID id) {
        if (!dutyRepo.existsById(id)) throw new EntityNotFoundException("duty " + id);
    }
}
