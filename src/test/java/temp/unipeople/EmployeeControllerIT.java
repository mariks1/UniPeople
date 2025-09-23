package temp.unipeople;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class EmployeeControllerIT extends BaseIntegrationTest {

  @Test
  void createAndList() throws Exception {
    var body =
        """
      {"firstName":"Иван","lastName":"Иванов","middleName":"Иваныч",
       "workEmail":"ivanov@uni.ru","phone":"+79000000000"}
      """;
    mvc.perform(post("/api/v1/employees").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());

    mvc.perform(get("/api/v1/employees?size=10&page=0"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Total-Count", Matchers.notNullValue()))
        .andExpect(jsonPath("$.content[0].workEmail").value("ivanov@uni.ru"));
  }
}
