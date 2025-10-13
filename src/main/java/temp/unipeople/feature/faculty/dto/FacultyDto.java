package temp.unipeople.feature.faculty.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class FacultyDto {

  UUID id;
  String code;
  String name;
}
