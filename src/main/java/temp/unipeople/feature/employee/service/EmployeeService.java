package temp.unipeople.feature.employee.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import temp.unipeople.feature.employee.dto.*;
import temp.unipeople.feature.employee.entity.Employee;
import temp.unipeople.feature.employee.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository repo;

  public EmployeeResponseDTO create(EmployeeCreateRequestDTO r) {
    var e =
        Employee.builder()
            .firstName(r.getFirstName())
            .lastName(r.getLastName())
            .middleName(r.getMiddleName())
            .workEmail(r.getWorkEmail())
            .phone(r.getPhone())
            .status(Employee.Status.ACTIVE)
            .build();
    return toDto(repo.save(e));
  }

  public Page<EmployeeResponseDTO> page(Pageable pageable) {
    return repo.findAll(pageable).map(this::toDto);
  }

  public Slice<EmployeeResponseDTO> slice(Instant cursor, int size) {
    var c = (cursor == null) ? Instant.now() : cursor;
    return repo.findByCreatedAtLessThanOrderByCreatedAtDescIdDesc(c, PageRequest.of(0, size))
        .map(this::toDto);
  }

  public EmployeeResponseDTO get(UUID id) {
    var e = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("employee not found"));
    return toDto(e);
  }

  private EmployeeResponseDTO toDto(Employee e) {
    return new EmployeeResponseDTO(
        e.getId(),
        e.getFirstName(),
        e.getLastName(),
        e.getMiddleName(),
        e.getWorkEmail(),
        e.getPhone(),
        e.getStatus().name());
  }
}
