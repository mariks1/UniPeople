package com.khasanshin.leaveservice.mapper;

import com.khasanshin.leaveservice.dto.*;
import com.khasanshin.leaveservice.entity.LeaveRequest;
import com.khasanshin.leaveservice.entity.LeaveType;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LeaveMapper {
  LeaveTypeDto toDto(LeaveType e);

  @Mapping(target = "id", ignore = true)
  LeaveType toEntity(CreateLeaveTypeDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  void updateEntity(UpdateLeaveTypeDto dto, @MappingTarget LeaveType e);

  LeaveRequestDto toDto(LeaveRequest e);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "approverId", ignore = true)
  @Mapping(
          target = "status",
          expression = "java(dto.isSubmit() ? LeaveRequest.Status.PENDING : LeaveRequest.Status.DRAFT)"
  )
  LeaveRequest toEntity(CreateLeaveRequestDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "employeeId", ignore = true)
  @Mapping(target = "typeId", ignore = true)
  @Mapping(target = "approverId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(UpdateLeaveRequestDto dto, @MappingTarget LeaveRequest e);
}
