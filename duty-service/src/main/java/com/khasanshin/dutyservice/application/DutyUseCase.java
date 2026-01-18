package com.khasanshin.dutyservice.application;

import com.khasanshin.dutyservice.dto.CreateDutyDto;
import com.khasanshin.dutyservice.dto.DutyDto;
import com.khasanshin.dutyservice.dto.UpdateDutyDto;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DutyUseCase {

    Page<DutyDto> findAll(Pageable pageable);

    DutyDto get(UUID id);

    DutyDto create(CreateDutyDto dto);

    DutyDto update(UUID id, UpdateDutyDto dto);

    void delete(UUID id);

    Page<DutyAssignmentDto> listAssignments(UUID dutyId, Pageable pageable);
}
