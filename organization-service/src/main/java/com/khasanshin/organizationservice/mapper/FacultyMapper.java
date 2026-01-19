package com.khasanshin.organizationservice.mapper;

import com.khasanshin.organizationservice.domain.model.Faculty;
import com.khasanshin.organizationservice.dto.CreateFacultyDto;
import com.khasanshin.organizationservice.dto.FacultyDto;
import com.khasanshin.organizationservice.dto.UpdateFacultyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FacultyMapper {

  FacultyDto toDto(Faculty faculty);

  @Mapping(target = "id", ignore = true)
  Faculty toDomain(CreateFacultyDto facultyDto);

  default Faculty updateDomain(UpdateFacultyDto dto, Faculty e) {
    Faculty.FacultyBuilder builder = e.toBuilder();
    if (dto.getName() != null) builder.name(dto.getName());
    if (dto.getCode() != null) builder.code(dto.getCode());
    return builder.build();
  }
}
