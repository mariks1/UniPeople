package temp.unipeople.feature.position.mapper;

import org.mapstruct.*;
import temp.unipeople.feature.position.dto.CreatePositionDto;
import temp.unipeople.feature.position.dto.PositionDto;
import temp.unipeople.feature.position.dto.UpdatePositionDto;
import temp.unipeople.feature.position.entity.Position;

@Mapper(componentModel = "spring")
public interface PositionMapper {

  PositionDto toDto(Position e);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Position toEntity(CreatePositionDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(UpdatePositionDto dto, @MappingTarget Position e);
}
