package temp.unipeople.feature.position.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.position.entity.Position;

public interface PositionRepository extends JpaRepository<Position, UUID> {

  boolean existsByNameIgnoreCase(String name);

  Page<Position> findByNameContainingIgnoreCase(
      String q, Pageable pageable); // опционально для поиска
}
