package temp.unipeople.feature.leave.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class DecisionDto {

  UUID approverId;

  @Size(max = 1000)
  String comment;
}
