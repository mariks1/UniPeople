package temp.unipeople.feature.position.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.position.entity.Position;

import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {}
