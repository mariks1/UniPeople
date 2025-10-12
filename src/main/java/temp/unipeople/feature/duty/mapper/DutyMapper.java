package temp.unipeople.feature.duty.mapper;

import org.mapstruct.*;
import temp.unipeople.feature.duty.dto.*;
import temp.unipeople.feature.duty.entity.Duty;

@Mapper(componentModel = "spring")
public interface DutyMapper {
  DutyDto toDto(Duty e);

  @Mapping(target = "id", ignore = true)
  Duty toEntity(CreateDutyRequest dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  void updateEntity(UpdateDutyRequest dto, @MappingTarget Duty e);
}
