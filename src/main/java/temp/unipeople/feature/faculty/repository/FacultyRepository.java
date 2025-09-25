package temp.unipeople.feature.faculty.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.faculty.entity.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {}
