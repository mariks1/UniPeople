package com.khasanshin.employeeservice.mapper;

import com.khasanshin.employeeservice.domain.model.Employee;
import com.khasanshin.employeeservice.dto.CreateEmployeeDto;
import com.khasanshin.employeeservice.dto.EmployeeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

  @Mapping(target = "departmentId", source = "department")
  EmployeeDto toDto(Employee entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "department", source = "departmentId")
  @Mapping(target = "status", constant = "ACTIVE")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Employee toDomain(CreateEmployeeDto dto);

}
