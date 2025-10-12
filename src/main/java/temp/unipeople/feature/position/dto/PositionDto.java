package temp.unipeople.feature.position.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class PositionDto {

  UUID id;
  String name;
  Instant createdAt;
  Instant updatedAt;
}
