package temp.unipeople.feature.faculty.mapper;

import org.mapstruct.*;
import temp.unipeople.feature.faculty.dto.CreateFacultyRequest;
import temp.unipeople.feature.faculty.dto.FacultyDto;
import temp.unipeople.feature.faculty.dto.UpdateFacultyRequest;
import temp.unipeople.feature.faculty.entity.Faculty;

@Mapper(componentModel = "spring")
public interface FacultyMapper {

  FacultyDto toDto(Faculty faculty);

  @Mapping(target = "id", ignore = true)
  Faculty toEntity(CreateFacultyRequest facultyDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  void updateEntity(UpdateFacultyRequest dto, @MappingTarget Faculty e);
}
