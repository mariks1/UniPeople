package com.khasanshin.dutyservice.mapper;

import com.khasanshin.dutyservice.domain.model.DutyAssignment;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DutyAssignmentMapper {

  DutyAssignmentDto toDto(DutyAssignment e);
}
