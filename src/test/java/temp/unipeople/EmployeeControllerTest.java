package temp.unipeople;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import temp.unipeople.feature.employee.controller.EmployeeController;
import temp.unipeople.feature.employee.dto.*;
import temp.unipeople.feature.employee.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@Import(EmployeeControllerTest.MockConfig.class) // подключаем конфиг с бином-моком
class EmployeeControllerTest {

  @TestConfiguration
  static class MockConfig {
    @Bean
    EmployeeService employeeService() {
      return Mockito.mock(EmployeeService.class);
    }
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;
  @Autowired EmployeeService service; // это наш мок из конфигурации

  @Test
  void findAll_returnsPage_andHeader() throws Exception {
    Page<EmployeeDto> page =
        new PageImpl<>(List.of(EmployeeDto.builder().build()), PageRequest.of(0, 10), 42);
    when(service.findAll(any())).thenReturn(page);

    var res =
        mvc.perform(get("/api/v1/employees"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "42"))
            .andReturn();

    assertThat(res.getResponse().getContentAsString()).isNotBlank();
  }

  @Test
  void stream_ok() throws Exception {
    Map<String, Object> payload = new HashMap<>();
    payload.put("items", List.of(EmployeeDto.builder().createdAt(Instant.now()).build()));
    payload.put("hasNext", true);
    payload.put("nextCursor", Instant.now());
    when(service.stream(any(), anyInt())).thenReturn(payload);

    mvc.perform(get("/api/v1/employees/stream").param("size", "2")).andExpect(status().isOk());
  }

  @Test
  void get_create_update_delete_fire_activate() throws Exception {
    UUID id = UUID.randomUUID();

    when(service.get(id)).thenReturn(EmployeeDto.builder().build());
    mvc.perform(get("/api/v1/employees/{id}", id)).andExpect(status().isOk());

    // <-- валидное тело для создания
    CreateEmployeeDto createBody =
        CreateEmployeeDto.builder()
            .firstName("A")
            .lastName("B")
            .middleName(null)
            .workEmail("a@uni.local")
            .phone("+79990000001")
            .departmentId(null)
            .build();

    when(service.create(any())).thenReturn(EmployeeDto.builder().build());
    mvc.perform(
            post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(createBody)))
        .andExpect(status().isCreated());

    when(service.update(eq(id), any())).thenReturn(EmployeeDto.builder().build());
    mvc.perform(
            put("/api/v1/employees/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(UpdateEmployeeDto.builder().firstName("B").build())))
        .andExpect(status().isOk());

    Mockito.doNothing().when(service).delete(id);
    mvc.perform(delete("/api/v1/employees/{id}", id)).andExpect(status().isNoContent());

    when(service.fire(id)).thenReturn(EmployeeDto.builder().build());
    mvc.perform(post("/api/v1/employees/{id}/fire", id)).andExpect(status().isOk());

    when(service.activate(id)).thenReturn(EmployeeDto.builder().build());
    mvc.perform(post("/api/v1/employees/{id}/activate", id)).andExpect(status().isOk());
  }
}
