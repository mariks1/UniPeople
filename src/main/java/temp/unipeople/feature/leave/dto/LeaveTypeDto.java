package temp.unipeople.feature.leave.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class LeaveTypeDto {

  private UUID id;
  private String name;
  private String code;
  boolean paid;
  Integer maxDaysPerYear;
}
