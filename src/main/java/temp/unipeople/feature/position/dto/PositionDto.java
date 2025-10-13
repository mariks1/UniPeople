package temp.unipeople.feature.position.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class PositionDto {

  UUID id;
  String name;
  Instant createdAt;
  Instant updatedAt;
}
