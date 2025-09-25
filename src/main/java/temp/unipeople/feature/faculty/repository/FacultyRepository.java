package temp.unipeople.feature.faculty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.faculty.entity.Faculty;
import java.util.UUID;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {}
