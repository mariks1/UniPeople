package temp.unipeople.feature.employee.repository;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.employee.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
  Slice<Employee> findByCreatedAtLessThanOrderByCreatedAtDescIdDesc(
      Instant cursor, Pageable pageable);
}
