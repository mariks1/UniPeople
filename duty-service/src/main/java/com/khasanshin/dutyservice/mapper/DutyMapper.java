package com.khasanshin.dutyservice.mapper;

import com.khasanshin.dutyservice.domain.model.Duty;
import com.khasanshin.dutyservice.dto.CreateDutyDto;
import com.khasanshin.dutyservice.dto.DutyDto;
import com.khasanshin.dutyservice.dto.UpdateDutyDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DutyMapper {
  DutyDto toDto(Duty e);

  @Mapping(target = "id", ignore = true)
  Duty toDomain(CreateDutyDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  Duty updateDomain(UpdateDutyDto dto, @MappingTarget Duty e);
}
