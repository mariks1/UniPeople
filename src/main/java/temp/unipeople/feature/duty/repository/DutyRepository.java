package temp.unipeople.feature.duty.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.duty.entity.Duty;

public interface DutyRepository extends JpaRepository<Duty, UUID> {}
