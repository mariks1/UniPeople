package temp.unipeople.feature.faculty.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFacultyRequest {

  @Size(max = 255)
  String name;

  @Size(max = 64)
  String code;
}
