package temp.unipeople.feature.employee.mapper;

import org.mapstruct.*;
import temp.unipeople.feature.employee.dto.CreateEmployeeRequest;
import temp.unipeople.feature.employee.dto.EmployeeDto;
import temp.unipeople.feature.employee.dto.UpdateEmployeeRequest;
import temp.unipeople.feature.employee.entity.Employee;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

  @Mapping(
      target = "departmentId",
      expression = "java(entity.getDepartment() != null ? entity.getDepartment().getId() : null)")
  EmployeeDto toDto(Employee entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "department", ignore = true)
  @Mapping(target = "status", constant = "ACTIVE")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Employee toEntity(CreateEmployeeRequest dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "department", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(UpdateEmployeeRequest dto, @MappingTarget Employee e);
}
