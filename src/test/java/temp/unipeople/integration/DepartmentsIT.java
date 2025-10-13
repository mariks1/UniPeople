package temp.unipeople.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
class DepartmentsIT extends BaseIntegrationTest {

  @Test
  void department_crud_and_head_flow_and_pagination() throws Exception {
    FacultyDto fac = createFaculty("FAC-1", "Physics");
    EmployeeDto head = createEmployee("Ivan", "Ivanov", "ivan@uni.local", "+79990000001");

    DepartmentDto dep = createDepartment("DEP-1", "Optics", fac.getId(), null);
    assertThat(dep.getId()).isNotNull();
    assertThat(dep.getFacultyId()).isEqualTo(fac.getId());
    assertThat(dep.getHeadEmployeeId()).isNull();

    DepartmentDto depRead = getDto("/api/v1/departments/{id}", DepartmentDto.class, dep.getId());
    assertThat(depRead.getId()).isEqualTo(dep.getId());

    MvcResult setHeadRes =
        mvc.perform(put("/api/v1/departments/{id}/head/{empId}", dep.getId(), head.getId()))
            .andExpect(status().isOk())
            .andReturn();
    DepartmentDto afterHead =
        om.readValue(setHeadRes.getResponse().getContentAsByteArray(), DepartmentDto.class);
    assertThat(afterHead.getHeadEmployeeId()).isEqualTo(head.getId());

    mvc.perform(delete("/api/v1/departments/{id}/head", dep.getId()))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/v1/departments"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));

    mvc.perform(get("/api/v1/departments/{id}/employees", dep.getId()))
        .andExpect(status().isNotImplemented());
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

  private EmployeeDto createEmployee(String first, String last, String email, String phone)
      throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("firstName", first);
    body.put("lastName", last);
    body.put("middleName", null);
    body.put("workEmail", email);
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

  private DepartmentDto createDepartment(String code, String name, UUID facId, UUID headId)
      throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("code", code);
    body.put("name", name);
    body.put("facultyId", facId);
    body.put("headEmployeeId", headId);
    MvcResult res =
        mvc.perform(
                post("/api/v1/departments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(body)))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), DepartmentDto.class);
  }

  private <T> T getDto(String url, Class<T> clazz, Object... vars) throws Exception {
    MvcResult res = mvc.perform(get(url, vars)).andExpect(status().isOk()).andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), clazz);
  }
}
