package temp.unipeople.feature.duty.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "duty")
public class Duty {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;
}
