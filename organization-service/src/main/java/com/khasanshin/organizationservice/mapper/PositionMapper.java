package com.khasanshin.organizationservice.mapper;

import com.khasanshin.organizationservice.domain.model.Position;
import com.khasanshin.organizationservice.dto.CreatePositionDto;
import com.khasanshin.organizationservice.dto.PositionDto;
import com.khasanshin.organizationservice.dto.UpdatePositionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PositionMapper {

  PositionDto toDto(Position e);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Position toDomain(CreatePositionDto dto);

  default Position updateDomain(UpdatePositionDto dto, Position e) {
      Position.PositionBuilder builder = e.toBuilder();
      if (dto.getName() != null) builder.name(dto.getName());
      return builder.build();
  }
}
