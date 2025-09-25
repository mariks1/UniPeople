package temp.unipeople.feature.department.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.department.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {}
