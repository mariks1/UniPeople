package temp.unipeople.feature.position.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "position")
@EntityListeners(AuditingEntityListener.class)
public class Position {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private java.time.Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private java.time.Instant updatedAt;
}
