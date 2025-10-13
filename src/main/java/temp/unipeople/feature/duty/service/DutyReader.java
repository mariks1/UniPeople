package temp.unipeople.feature.duty.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import temp.unipeople.feature.duty.entity.Duty;
import temp.unipeople.feature.duty.repository.DutyRepository;

@Service
@RequiredArgsConstructor
public class DutyReader {
  private final DutyRepository repo;
  private final EntityManager em;

  public Duty require(UUID id) {
    return repo.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("duty not found: " + id));
  }

  public Duty getRef(UUID id) {
    return em.getReference(Duty.class, id);
  }
}
