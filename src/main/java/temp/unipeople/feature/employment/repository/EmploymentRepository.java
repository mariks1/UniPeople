package temp.unipeople.feature.employment.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.employment.entity.Employment;

public interface EmploymentRepository extends JpaRepository<Employment, UUID> {}
