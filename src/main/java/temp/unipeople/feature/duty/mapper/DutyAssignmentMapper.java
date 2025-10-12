package temp.unipeople.feature.duty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import temp.unipeople.feature.duty.dto.DutyAssignmentDto;
import temp.unipeople.feature.duty.entity.DepartmentDutyAssignment;

@Mapper(componentModel = "spring")
public interface DutyAssignmentMapper {

  @Mapping(source = "department.id", target = "departmentId")
  @Mapping(source = "employee.id", target = "employeeId")
  @Mapping(source = "duty.id", target = "dutyId")
  @Mapping(source = "assignedBy.id", target = "assignedBy")
  DutyAssignmentDto toDto(DepartmentDutyAssignment e);
}
