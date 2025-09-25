package temp.unipeople.feature.leave.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.leave.request.entity.LeaveRequest;

import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {}
