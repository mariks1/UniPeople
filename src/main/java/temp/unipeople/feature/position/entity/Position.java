package temp.unipeople.feature.position.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "position")
public class Position {

  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private String name;
}
