package temp.unipeople.feature.leave.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

@Data
public class DecisionDto {

  UUID approverId;

  @Size(max = 1000)
  String comment;
}
