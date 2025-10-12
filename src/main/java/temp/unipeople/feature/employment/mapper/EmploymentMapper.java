package temp.unipeople.feature.employment.mapper;

import org.mapstruct.*;
import temp.unipeople.feature.employment.dto.*;
import temp.unipeople.feature.employment.entity.Employment;

@Mapper(componentModel = "spring")
public interface EmploymentMapper {

  EmploymentDto toDto(Employment e);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", constant = "ACTIVE")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Employment toEntity(CreateEmploymentDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "employeeId", ignore = true)
  @Mapping(target = "departmentId", ignore = true)
  @Mapping(target = "positionId", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(UpdateEmploymentDto dto, @MappingTarget Employment e);
}
