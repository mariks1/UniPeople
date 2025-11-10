package com.khasanshin.organizationservice.mapper;

import com.khasanshin.organizationservice.dto.CreateFacultyDto;
import com.khasanshin.organizationservice.dto.FacultyDto;
import com.khasanshin.organizationservice.dto.UpdateFacultyDto;
import com.khasanshin.organizationservice.entity.Faculty;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FacultyMapper {

  FacultyDto toDto(Faculty faculty);

  @Mapping(target = "id", ignore = true)
  Faculty toEntity(CreateFacultyDto facultyDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  void updateEntity(UpdateFacultyDto dto, @MappingTarget Faculty e);
}
