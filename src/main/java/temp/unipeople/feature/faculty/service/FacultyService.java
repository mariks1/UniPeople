package temp.unipeople.feature.faculty.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temp.unipeople.feature.faculty.dto.CreateFacultyDto;
import temp.unipeople.feature.faculty.dto.FacultyDto;
import temp.unipeople.feature.faculty.dto.UpdateFacultyDto;
import temp.unipeople.feature.faculty.entity.Faculty;
import temp.unipeople.feature.faculty.mapper.FacultyMapper;
import temp.unipeople.feature.faculty.repository.FacultyRepository;

@Service
@RequiredArgsConstructor
public class FacultyService {

  private final FacultyRepository repo;
  private final FacultyMapper mapper;

  public Page<FacultyDto> page(Pageable pageable) {
    Pageable sorted = pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Order.asc("code"), Sort.Order.asc("name")));
    return repo.findAll(sorted).map(mapper::toDto);
  }

  public FacultyDto get(UUID id) {
    Faculty d =
        repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));
    return mapper.toDto(d);
  }

  @Transactional
  public FacultyDto create(CreateFacultyDto dto) {
    Faculty faculty = mapper.toEntity(dto);
    return mapper.toDto(repo.save(faculty));
  }

  @Transactional
  public FacultyDto update(UUID id, UpdateFacultyDto dto) {
    Faculty e =
        repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("faculty not found: " + id));

    mapper.updateEntity(dto, e);
    return mapper.toDto(e);
  }
}
