package temp.unipeople.feature.leave.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.leave.entity.LeaveType;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {
  boolean existsByCodeIgnoreCase(String code);
}
