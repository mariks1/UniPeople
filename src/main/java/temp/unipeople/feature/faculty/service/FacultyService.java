package temp.unipeople.feature.faculty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import temp.unipeople.feature.faculty.entity.Faculty;
import temp.unipeople.feature.faculty.repository.FacultyRepository;

import java.util.UUID;

@Service @RequiredArgsConstructor
public class FacultyService {
    private final FacultyRepository repo;

    public Page<Faculty> page(Pageable pageable) { return Page.empty(pageable); } // TODO impl
    public Faculty get(UUID id) { throw new UnsupportedOperationException("TODO"); }
    public Faculty create(Faculty f) { throw new UnsupportedOperationException("TODO"); }
}

