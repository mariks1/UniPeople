package temp.unipeople.feature.leave.mapper;

import org.mapstruct.*;
import temp.unipeople.feature.leave.dto.CreateLeaveRequestDto;
import temp.unipeople.feature.leave.dto.CreateLeaveTypeDto;
import temp.unipeople.feature.leave.dto.LeaveRequestDto;
import temp.unipeople.feature.leave.dto.LeaveTypeDto;
import temp.unipeople.feature.leave.dto.UpdateLeaveRequestDto;
import temp.unipeople.feature.leave.dto.UpdateLeaveTypeDto;
import temp.unipeople.feature.leave.entity.LeaveRequest;
import temp.unipeople.feature.leave.entity.LeaveType;

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
  @Mapping(
      target = "status",
      expression = "java(dto.isSubmit() ? LeaveRequest.Status.PENDING : LeaveRequest.Status.DRAFT)")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  LeaveRequest toEntity(CreateLeaveRequestDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "employeeId", ignore = true)
  @Mapping(target = "typeId", ignore = true)
  void updateEntity(UpdateLeaveRequestDto dto, @MappingTarget LeaveRequest e);
}
