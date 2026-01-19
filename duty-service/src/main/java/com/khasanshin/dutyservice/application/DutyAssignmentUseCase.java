package com.khasanshin.dutyservice.application;

import com.khasanshin.dutyservice.dto.AssignDutyDto;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DutyAssignmentUseCase {

    DutyAssignmentDto assign(UUID departmentId, AssignDutyDto req);

    Page<DutyAssignmentDto> list(UUID departmentId, Pageable pageable);

    void unassign(UUID departmentId, UUID assignmentId);

    DutyAssignmentDto unassignAndReturn(UUID departmentId, UUID assignmentId);
}
