package temp.unipeople.feature.department.entity;

import org.mapstruct.Mapper;
import temp.unipeople.feature.department.dto.DepartmentDto;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentDto toDto(Department entity);


}
