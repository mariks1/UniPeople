package temp.unipeople.feature.duty.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.duty.entity.DepartmentDutyAssignment;

public interface DepartmentDutyAssignmentRepository
    extends JpaRepository<DepartmentDutyAssignment, UUID> {

    Page<DepartmentDutyAssignment> findByDepartmentId(UUID departmentId, Pageable pageable);

    boolean existsByDepartmentIdAndEmployeeIdAndDutyId(UUID departmentId, UUID employeeId, UUID dutyId);

}
