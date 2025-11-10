package com.khasanshin.dutyservice.mapper;

import com.khasanshin.dutyservice.dto.CreateDutyDto;
import com.khasanshin.dutyservice.dto.DutyDto;
import com.khasanshin.dutyservice.dto.UpdateDutyDto;
import com.khasanshin.dutyservice.entity.Duty;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DutyMapper {
  DutyDto toDto(Duty e);

  @Mapping(target = "id", ignore = true)
  Duty toEntity(CreateDutyDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  void updateEntity(UpdateDutyDto dto, @MappingTarget Duty e);
}
