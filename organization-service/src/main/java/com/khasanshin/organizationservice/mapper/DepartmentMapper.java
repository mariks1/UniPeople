package com.khasanshin.organizationservice.mapper;

import com.khasanshin.organizationservice.domain.model.Department;
import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

  DepartmentDto toDto(Department entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "facultyId", source = "facultyId")
  @Mapping(target = "headEmployeeId", source = "headEmployeeId")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Department toDomain(CreateDepartmentDto dto);

  default Department updateDomain(UpdateDepartmentDto dto, Department e) {
      Department.DepartmentBuilder builder = e.toBuilder();
      if (dto.getName() != null) builder.name(dto.getName());
      if (dto.getCode() != null) builder.code(dto.getCode());
      if (dto.getFacultyId() != null) builder.facultyId(dto.getFacultyId());
      if (dto.getHeadEmployeeId() != null) builder.headEmployeeId(dto.getHeadEmployeeId());
      return builder.build();
  }
}
