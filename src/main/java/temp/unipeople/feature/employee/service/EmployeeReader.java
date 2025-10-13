package temp.unipeople.feature.employee.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import temp.unipeople.feature.employee.entity.Employee;
import temp.unipeople.feature.employee.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class EmployeeReader {
  private final EmployeeRepository repo;
  private final EntityManager em;

  public Employee require(UUID id) {
    return repo.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));
  }

  public Employee getRef(UUID id) {
    return em.getReference(Employee.class, id);
  }
}
