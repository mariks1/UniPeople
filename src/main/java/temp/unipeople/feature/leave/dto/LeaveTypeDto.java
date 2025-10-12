package temp.unipeople.feature.leave.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class LeaveTypeDto {

  private UUID id;
  private String name;
  private String code;
  boolean paid;
  Integer maxDaysPerYear;
}
