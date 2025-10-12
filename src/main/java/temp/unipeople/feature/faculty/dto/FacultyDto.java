package temp.unipeople.feature.faculty.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultyDto {

  UUID id;
  String code;
  String name;
}
