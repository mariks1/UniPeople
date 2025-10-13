package temp.unipeople.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import temp.unipeople.feature.department.dto.DepartmentDto;
import temp.unipeople.feature.employee.dto.EmployeeDto;
import temp.unipeople.feature.employment.dto.EmploymentDto;
import temp.unipeople.feature.faculty.dto.FacultyDto;
import temp.unipeople.feature.position.dto.PositionDto;

@ActiveProfiles("test")
class EmploymentsIT extends BaseIntegrationTest {

  @Test
  void employment_create_list_close() throws Exception {
    EmployeeDto emp = createEmployee("Kate", "Brown", "kate@uni.local", "+79990000031");
    FacultyDto fac = createFaculty("FAC-3", "Engineering");
    DepartmentDto dep = createDepartment("DEP-3", "Mechanics", fac.getId(), null);
    PositionDto pos = createPosition("Engineer");

    Map<String, Object> body = new HashMap<>();
    body.put("employee_id", emp.getId());
    body.put("department_id", dep.getId());
    body.put("position_id", pos.getId());
    body.put("start_date", LocalDate.now().toString());
    body.put("end_date", null);
    body.put("rate", new BigDecimal("1.0"));
    body.put("salary", 1000);

    MvcResult created =
        mvc.perform(
                post("/api/v1/employments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(body)))
            .andExpect(status().isCreated())
            .andReturn();
    EmploymentDto dto =
        om.readValue(created.getResponse().getContentAsByteArray(), EmploymentDto.class);

    mvc.perform(get("/api/v1/employments/by-employee/{id}", emp.getId()))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));

    mvc.perform(get("/api/v1/employments/by-department/{id}", dep.getId()))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));

    mvc.perform(
            post("/api/v1/employments/{id}/close", dto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isOk());
  }

  private EmployeeDto createEmployee(String first, String last, String email, String phone)
      throws Exception {
    Map<String, Object> b = new HashMap<>();
    b.put("first_name", first);
    b.put("last_name", last);
    b.put("middle_name", null);
    b.put("work_email", email);
    b.put("phone", phone);
    MvcResult res =
        mvc.perform(
                post("/api/v1/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(b)))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), EmployeeDto.class);
  }

  private FacultyDto createFaculty(String code, String name) throws Exception {
    MvcResult res =
        mvc.perform(
                post("/api/v1/faculties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("code", code, "name", name))))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), FacultyDto.class);
  }

  private DepartmentDto createDepartment(String code, String name, UUID facId, UUID headId)
      throws Exception {
    Map<String, Object> b = new HashMap<>();
    b.put("code", code);
    b.put("name", name);
    b.put("faculty_id", facId);
    b.put("head_employee_id", headId);
    MvcResult res =
        mvc.perform(
                post("/api/v1/departments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(b)))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), DepartmentDto.class);
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
