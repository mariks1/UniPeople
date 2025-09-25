package temp.unipeople.feature.leave.type.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.leave.type.entity.LeaveType;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {}
