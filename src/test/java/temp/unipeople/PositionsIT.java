package temp.unipeople;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import temp.unipeople.feature.position.dto.PositionDto;

@ActiveProfiles("test")
class PositionsIT extends BaseIntegrationTest {

  @Test
  void position_crud_and_search() throws Exception {
    // create
    PositionDto p1 = createPosition("Developer");
    PositionDto p2 = createPosition("DevOps");
    assertThat(p1.getId()).isNotNull();
    assertThat(p2.getId()).isNotNull();

    // get
    mvc.perform(get("/api/v1/positions/{id}", p1.getId())).andExpect(status().isOk());

    // list (q + X-Total-Count)
    mvc.perform(get("/api/v1/positions").param("q", "dev"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));

    // update
    Map<String, Object> upd = Map.of("name", "Senior Developer");
    MvcResult updated =
        mvc.perform(
                put("/api/v1/positions/{id}", p1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(upd)))
            .andExpect(status().isOk())
            .andReturn();
    PositionDto after =
        om.readValue(updated.getResponse().getContentAsByteArray(), PositionDto.class);
    assertThat(after.getName()).isEqualTo("Senior Developer");

    // delete
    mvc.perform(delete("/api/v1/positions/{id}", p2.getId())).andExpect(status().isNoContent());
    // get deleted -> 404
    mvc.perform(get("/api/v1/positions/{id}", p2.getId())).andExpect(status().isNotFound());
  }

  private PositionDto createPosition(String name) throws Exception {
    MvcResult res =
        mvc.perform(
                post("/api/v1/positions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("name", name))))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), PositionDto.class);
  }
}
