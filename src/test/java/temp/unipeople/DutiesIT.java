package temp.unipeople;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import temp.unipeople.feature.duty.dto.DutyDto;

@ActiveProfiles("test")
class DutiesIT extends BaseIntegrationTest {

  @Test
  void duty_create_and_list_assignments() throws Exception {
    DutyDto duty = createDuty("DUTY-1", "Advising");
    // GET by id
    mvc.perform(get("/api/v1/duties/{id}", duty.getId())).andExpect(status().isOk());

    // list duties (pagination header)
    mvc.perform(get("/api/v1/duties"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));

    // list assignments by duty id
    mvc.perform(get("/api/v1/duties/{id}/assignments", duty.getId()))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));

    // второй эндпоинт из контроллера (в нём странный путь, но он есть) — вернёт пустую страницу
    mvc.perform(get("/api/v1/duties/{id}/duties/assignments", duty.getId()))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));
  }

  private DutyDto createDuty(String code, String name) throws Exception {
    MvcResult res =
        mvc.perform(
                post("/api/v1/duties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("code", code, "name", name))))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), DutyDto.class);
  }
}
