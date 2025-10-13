package temp.unipeople.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import temp.unipeople.feature.department.dto.DepartmentDto;
import temp.unipeople.feature.employee.dto.EmployeeDto;
import temp.unipeople.feature.faculty.dto.FacultyDto;

@ActiveProfiles("test")
class EmployeesIT extends BaseIntegrationTest {

  @Test
  void employee_full_flow_stream_and_404() throws Exception {
    EmployeeDto e1 = createEmployee("Ann", "Smith", "ann@uni.local", "+79990000011");
    EmployeeDto e2 = createEmployee("Petr", "Petrov", "petr@uni.local", "+79990000012");

    MvcResult stream =
        mvc.perform(get("/api/v1/employees/stream").param("size", "1"))
            .andExpect(status().isOk())
            .andReturn();
    Map<String, Object> body =
        om.readValue(
            stream.getResponse().getContentAsByteArray(),
            new TypeReference<Map<String, Object>>() {});
    assertThat(body).containsKeys("items", "hasNext", "nextCursor");

    FacultyDto fac = createFaculty("FAC-2", "Math");
    DepartmentDto dep = createDepartment("DEP-2", "Algebra", fac.getId(), null);
    Map<String, Object> patch = Map.of("department_id", dep.getId());
    MvcResult upd =
        mvc.perform(
                put("/api/v1/employees/{id}", e2.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(patch)))
            .andExpect(status().isOk())
            .andReturn();
    EmployeeDto e2Upd = om.readValue(upd.getResponse().getContentAsByteArray(), EmployeeDto.class);
    assertThat(e2Upd.getDepartmentId()).isEqualTo(dep.getId());

    MvcResult fired =
        mvc.perform(post("/api/v1/employees/{id}/fire", e2.getId()))
            .andExpect(status().isOk())
            .andReturn();
    EmployeeDto afterFire =
        om.readValue(fired.getResponse().getContentAsByteArray(), EmployeeDto.class);
    assertThat(afterFire.getStatus().name()).isEqualTo("FIRED");

    MvcResult activated =
        mvc.perform(post("/api/v1/employees/{id}/activate", e2.getId()))
            .andExpect(status().isOk())
            .andReturn();

    mvc.perform(get("/api/v1/employees/{id}", e1.getId())).andExpect(status().isOk());
    mvc.perform(get("/api/v1/employees/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
  }

  private EmployeeDto createEmployee(String first, String last, String email, String phone)
      throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("first_name", first);
    body.put("last_name", last);
    body.put("middle_name", null);
    body.put("work_email", email);
    body.put("phone", phone);
    MvcResult res =
        mvc.perform(
                post("/api/v1/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(body)))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), EmployeeDto.class);
  }

  private FacultyDto createFaculty(String code, String name) throws Exception {
    Map<String, Object> body = Map.of("code", code, "name", name);
    MvcResult res =
        mvc.perform(
                post("/api/v1/faculties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(body)))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), FacultyDto.class);
  }

  private DepartmentDto createDepartment(String code, String name, UUID facId, UUID headId)
      throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("code", code);
    body.put("name", name);
    body.put("faculty_id", facId);
    body.put("head_employee_id", headId);
    MvcResult res =
        mvc.perform(
                post("/api/v1/departments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(body)))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), DepartmentDto.class);
  }
}
