package com.khasanshin.leaveservice.mapper;

import com.khasanshin.leaveservice.domain.model.LeaveRequest;
import com.khasanshin.leaveservice.domain.model.LeaveType;
import com.khasanshin.leaveservice.dto.*;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LeaveMapper {
  LeaveTypeDto toDto(LeaveType e);

  @Mapping(target = "id", ignore = true)
  LeaveType toDomain(CreateLeaveTypeDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  LeaveType updateLeaveType(UpdateLeaveTypeDto dto, @MappingTarget LeaveType e);

  LeaveRequestDto toDto(LeaveRequest e);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "approverId", ignore = true)
  @Mapping(
          target = "status",
          expression = "java(dto.isSubmit() ? LeaveRequest.Status.PENDING : LeaveRequest.Status.DRAFT)"
  )
  LeaveRequest toDomain(CreateLeaveRequestDto dto);

  default LeaveRequest updateLeave(UpdateLeaveRequestDto dto, @MappingTarget LeaveRequest e) {
      LeaveRequest.LeaveRequestBuilder builder = e.toBuilder();
      if (dto.getDateFrom() != null) builder.dateFrom(dto.getDateFrom());
      if (dto.getDateTo() != null) builder.dateTo(dto.getDateTo());
      if (Objects.nonNull(dto.getComment())) builder.comment(dto.getComment());
      return builder.build();
  }
}
