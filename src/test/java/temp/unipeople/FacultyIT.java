package temp.unipeople;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import temp.unipeople.feature.faculty.dto.FacultyDto;

@ActiveProfiles("test")
class FacultyIT extends BaseIntegrationTest {

  @Test
  void faculty_create_get_update_and_conflict() throws Exception {
    // create
    FacultyDto created = createFaculty("FAC-100", "Math");

    // get by id
    mvc.perform(get("/api/v1/faculties/{id}", created.getId())).andExpect(status().isOk());

    // update
    Map<String, Object> upd = new HashMap<>();
    upd.put("name", "Mathematics");
    mvc.perform(
            put("/api/v1/faculties/{id}", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(upd)))
        .andExpect(status().isOk());

    // conflict on duplicate code
    createFaculty("FAC-200", "Physics");
    Map<String, Object> dup = new HashMap<>();
    dup.put("code", "FAC-200");
    dup.put("name", "Another");
    mvc.perform(
            post("/api/v1/faculties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(dup)))
        .andExpect(status().isConflict());

    // 404 on unknown id for GET (проверяем Error path контроллера)
    mvc.perform(get("/api/v1/faculties/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
  }

  private FacultyDto createFaculty(String code, String name) throws Exception {
    Map<String, Object> b = new HashMap<>();
    b.put("code", code);
    b.put("name", name);

    MvcResult res =
        mvc.perform(
                post("/api/v1/faculties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(b)))
            .andExpect(status().isCreated())
            .andReturn();

    return om.readValue(res.getResponse().getContentAsByteArray(), FacultyDto.class);
  }
}
