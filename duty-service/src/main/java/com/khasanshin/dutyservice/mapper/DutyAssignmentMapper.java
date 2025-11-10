package com.khasanshin.dutyservice.mapper;

import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import com.khasanshin.dutyservice.entity.DepartmentDutyAssignment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DutyAssignmentMapper {

  DutyAssignmentDto toDto(DepartmentDutyAssignment e);
}
