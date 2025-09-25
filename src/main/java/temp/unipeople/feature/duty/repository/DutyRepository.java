package temp.unipeople.feature.duty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.duty.entity.Duty;

import java.util.UUID;

public interface DutyRepository extends JpaRepository<Duty, UUID> {
}
