package temp.unipeople.feature.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.department.entity.Department;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {}
