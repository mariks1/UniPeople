package temp.unipeople.feature.position.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.position.entity.Position;

public interface PositionRepository extends JpaRepository<Position, UUID> {}
