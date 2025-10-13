package temp.unipeople.feature.leave.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class DecisionDto {

  @JsonAlias("approver_id")
  @JsonProperty("approver_id")
  UUID approverId;

  @Size(max = 1000)
  String comment;
}
