package com.khasanshin.organizationservice.mapper;

import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import com.khasanshin.organizationservice.entity.Department;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface DepartmentMapper {

  @Mapping(target = "facultyId", source = "faculty")
  @Mapping(target = "headEmployeeId", source = "headEmployee")
  DepartmentDto toDto(Department entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "faculty", source = "facultyId")
  @Mapping(target = "headEmployee", source = "headEmployeeId")
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
