package temp.unipeople.feature.employment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.employment.entity.Employment;

import java.util.UUID;

public interface EmploymentRepository extends JpaRepository<Employment, UUID> {}

