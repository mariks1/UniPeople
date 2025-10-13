package temp.unipeople.feature.department.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temp.unipeople.feature.department.entity.Department;
import temp.unipeople.feature.department.repository.DepartmentRepository;

@Service
@RequiredArgsConstructor
public class DepartmentReader {
  private final DepartmentRepository repo;
  private final EntityManager em;

  @Transactional(readOnly = true)
  public Department require(UUID id) {
    return repo.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("department not found: " + id));
  }

  @Transactional
  public void clearHeadByEmployeeId(UUID headEmployeeId) {
    repo.clearHeadByEmployeeId(headEmployeeId);
  }

  @Transactional(readOnly = true)
  public Department getRef(UUID id) {
    return em.getReference(Department.class, id);
  }
}
