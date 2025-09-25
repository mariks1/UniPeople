package temp.unipeople.feature.leave.type.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.leave.type.entity.LeaveType;

import java.util.UUID;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {}
