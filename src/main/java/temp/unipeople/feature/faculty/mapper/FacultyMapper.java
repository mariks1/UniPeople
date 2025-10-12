package temp.unipeople.feature.faculty.mapper;

import org.mapstruct.*;
import temp.unipeople.feature.faculty.dto.CreateFacultyDto;
import temp.unipeople.feature.faculty.dto.FacultyDto;
import temp.unipeople.feature.faculty.dto.UpdateFacultyDto;
import temp.unipeople.feature.faculty.entity.Faculty;

@Mapper(componentModel = "spring")
public interface FacultyMapper {

  FacultyDto toDto(Faculty faculty);

  @Mapping(target = "id", ignore = true)
  Faculty toEntity(CreateFacultyDto facultyDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  void updateEntity(UpdateFacultyDto dto, @MappingTarget Faculty e);
}
