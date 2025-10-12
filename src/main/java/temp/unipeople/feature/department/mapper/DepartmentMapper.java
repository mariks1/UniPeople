package temp.unipeople.feature.department.mapper;

import org.mapstruct.*;
import temp.unipeople.feature.department.dto.CreateDepartmentDto;
import temp.unipeople.feature.department.dto.DepartmentDto;
import temp.unipeople.feature.department.dto.UpdateDepartmentDto;
import temp.unipeople.feature.department.entity.Department;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

  @Mapping(
      target = "facultyId",
      expression = "java(entity.getFaculty() != null ? entity.getFaculty().getId() : null)")
  @Mapping(
      target = "headEmployeeId",
      expression =
          "java(entity.getHeadEmployee() != null ? entity.getHeadEmployee().getId() : null)")
  DepartmentDto toDto(Department entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "faculty", ignore = true)
  @Mapping(target = "headEmployee", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Department toEntity(CreateDepartmentDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "faculty", ignore = true)
  @Mapping(target = "headEmployee", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(UpdateDepartmentDto dto, @MappingTarget Department e);
}
