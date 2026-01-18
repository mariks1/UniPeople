package com.khasanshin.employmentservice.mapper;

import com.khasanshin.employmentservice.domain.model.Employment;
import com.khasanshin.employmentservice.dto.CreateEmploymentDto;
import com.khasanshin.employmentservice.dto.EmploymentDto;
import com.khasanshin.employmentservice.dto.UpdateEmploymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmploymentMapper {

  EmploymentDto toDto(Employment e);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", constant = "ACTIVE")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "endDate", ignore = true)
  Employment toDomain(CreateEmploymentDto dto);

  default Employment applyUpdates(Employment current, UpdateEmploymentDto dto) {
      Employment.EmploymentBuilder builder = current.toBuilder();
      if (dto.getSalary() != null) builder.salary(dto.getSalary());
      if (dto.getRate() != null) builder.rate(dto.getRate());
      if (dto.getEndDate() != null) builder.endDate(dto.getEndDate());
      return builder.build();
  }
}
