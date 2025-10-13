package temp.unipeople;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import temp.unipeople.feature.department.dto.DepartmentDto;
import temp.unipeople.feature.employee.dto.EmployeeDto;
import temp.unipeople.feature.faculty.dto.FacultyDto;

/**
 * Сценарий: 1) создать двух сотрудников 2) создать два факультета 3) создать департамент, привязать
 * к faculty1 и назначить head = employee1 4) присвоить департамент employee2 (PUT /employees/{id} с
 * departmentId)
 */
@ActiveProfiles("test")
class DepartmentFlowIT extends BaseIntegrationTest {

  @Autowired ObjectMapper om;

  @Test
  void createEmployeesFacultiesDepartmentAndAssign() throws Exception {
    // 1) employees
    EmployeeDto emp1 = createEmployee("Ivan", "Ivanov", "ivan.ivanov@uni.local", "+79990000001");
    EmployeeDto emp2 = createEmployee("Petr", "Petrov", "petr.petrov@uni.local", "+79990000002");

    // 2) faculties
    FacultyDto fac1 = createFaculty("Physics", "PHY");
    FacultyDto fac2 = createFaculty("Mathematics", "MTH");
    assertThat(fac1.getId()).isNotNull();
    assertThat(fac2.getId()).isNotNull();
    assertThat(fac1.getId()).isNotEqualTo(fac2.getId());

    // 3) department (faculty = fac1, head = emp1)
    DepartmentDto dep =
        createDepartment("DEP-01", "Department of Optics", fac1.getId(), emp1.getId());
    assertThat(dep.getFacultyId()).isEqualTo(fac1.getId());
    assertThat(dep.getHeadEmployeeId()).isEqualTo(emp1.getId());

    // верифицируем GET департамента
    DepartmentDto depRead = getDto("/api/v1/departments/{id}", DepartmentDto.class, dep.getId());
    assertThat(depRead.getId()).isEqualTo(dep.getId());
    assertThat(depRead.getFacultyId()).isEqualTo(fac1.getId());
    assertThat(depRead.getHeadEmployeeId()).isEqualTo(emp1.getId());

    // 4) присвоить департамент employee2
    // Предполагаем, что UpdateEmployeeDto допускает частичный JSON с полем departmentId
    Map<String, Object> updateEmp2 = Map.of("departmentId", dep.getId());
    MvcResult putEmp2 =
        mvc.perform(
                put("/api/v1/employees/{id}", emp2.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updateEmp2)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();

    EmployeeDto emp2Updated =
        om.readValue(putEmp2.getResponse().getContentAsByteArray(), EmployeeDto.class);
    assertThat(emp2Updated.getDepartmentId()).isEqualTo(dep.getId());

    // контрольный GET сотрудника 2
    EmployeeDto emp2Read = getDto("/api/v1/employees/{id}", EmployeeDto.class, emp2.getId());
    assertThat(emp2Read.getDepartmentId()).isEqualTo(dep.getId());
  }

  // ===== helpers =====

  private EmployeeDto createEmployee(String first, String last, String email, String phone)
      throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("firstName", first);
    body.put("lastName", last);
    body.put("middleName", null);
    body.put("workEmail", email);
    body.put("phone", phone);
    // departmentId не задаём при создании

    MvcResult res =
        mvc.perform(
                post("/api/v1/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(body)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();

    return om.readValue(res.getResponse().getContentAsByteArray(), EmployeeDto.class);
  }

  private FacultyDto createFaculty(String name, String code) throws Exception {
    Map<String, Object> body = Map.of("name", name, "code", code);

    MvcResult res =
        mvc.perform(
                post("/api/v1/faculties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(body)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();

    return om.readValue(res.getResponse().getContentAsByteArray(), FacultyDto.class);
  }

  private DepartmentDto createDepartment(
      String code, String name, UUID facultyId, UUID headEmployeeId) throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("code", code);
    body.put("name", name);
    body.put("facultyId", facultyId);
    body.put("headEmployeeId", headEmployeeId);

    MvcResult res =
        mvc.perform(
                post("/api/v1/departments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(body)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn();

    return om.readValue(res.getResponse().getContentAsByteArray(), DepartmentDto.class);
  }

  private <T> T getDto(String url, Class<T> clazz, Object... uriVars) throws Exception {
    MvcResult res =
        mvc.perform(get(url, uriVars).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), clazz);
  }
}
