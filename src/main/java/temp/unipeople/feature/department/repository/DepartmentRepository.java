package temp.unipeople.feature.department.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import temp.unipeople.feature.department.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
  @Modifying
  @Query("update Department d set d.headEmployee = null where d.headEmployee.id = :employeeId")
  void clearHeadByEmployeeId(@Param("employeeId") UUID employeeId);
}
