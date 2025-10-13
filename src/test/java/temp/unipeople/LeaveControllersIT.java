package temp.unipeople;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import temp.unipeople.feature.employee.dto.EmployeeDto;
import temp.unipeople.feature.leave.dto.LeaveRequestDto;
import temp.unipeople.feature.leave.dto.LeaveTypeDto;

@ActiveProfiles("test")
class LeaveControllersIT extends BaseIntegrationTest {

  @Test
  void leave_types_crud_and_requests_flows() throws Exception {
    // ---- Employees & Types ----
    EmployeeDto emp = createEmployee("Oleg", "Orlov", "oleg@uni.local", "+79990000041");

    LeaveTypeDto typeForUse = createLeaveType("VAC", "Vacation", true, 30);

    // list types with X-Total-Count
    mvc.perform(get("/api/v1/leave-types"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));

    // update type
    Map<String, Object> updType = new HashMap<>();
    updType.put("name", "Vacation (Paid)");
    mvc.perform(
            patch("/api/v1/leave-types/{id}", typeForUse.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(updType)))
        .andExpect(status().isOk());

    // create a throwaway type and delete it (204)
    LeaveTypeDto toDelete = createLeaveType("SICK", "Sick", true, 15);
    mvc.perform(delete("/api/v1/leave-types/{id}", toDelete.getId()))
        .andExpect(status().isNoContent());

    // ---- Requests (valid) ----
    Map<String, Object> req = new HashMap<>();
    req.put("employeeId", emp.getId());
    req.put("typeId", typeForUse.getId());
    req.put("dateFrom", LocalDate.now().plusDays(3).toString());
    req.put("dateTo", LocalDate.now().plusDays(5).toString());

    MvcResult created =
        mvc.perform(
                post("/api/v1/leave-requests")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(req)))
            .andExpect(status().isCreated())
            .andReturn();

    LeaveRequestDto lr =
        om.readValue(created.getResponse().getContentAsByteArray(), LeaveRequestDto.class);

    // GET by id
    mvc.perform(get("/api/v1/leave-requests/{id}", lr.getId())).andExpect(status().isOk());

    // list by employee (X-Total-Count)
    mvc.perform(get("/api/v1/leave-requests/by-employee/{empId}", emp.getId()))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));

    // list by status
    JsonNode node = om.readTree(created.getResponse().getContentAsByteArray());
    String status = node.get("status").asText();
    mvc.perform(get("/api/v1/leave-requests").param("status", status))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"));

    // try delete type that is referenced by a request -> expect 409
    mvc.perform(delete("/api/v1/leave-types/{id}", typeForUse.getId()))
        .andExpect(status().isConflict());

    // ---- Requests (invalid) : dateFrom > dateTo -> 400 ----
    Map<String, Object> badReq = new HashMap<>();
    badReq.put("employeeId", emp.getId());
    badReq.put("typeId", typeForUse.getId());
    badReq.put("dateFrom", LocalDate.now().plusDays(10).toString());
    badReq.put("dateTo", LocalDate.now().plusDays(5).toString());

    mvc.perform(
            post("/api/v1/leave-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(badReq)))
        .andExpect(status().isBadRequest());
  }

  // ===== helpers =====

  private EmployeeDto createEmployee(String first, String last, String email, String phone)
      throws Exception {
    Map<String, Object> b = new HashMap<>();
    b.put("firstName", first);
    b.put("lastName", last);
    b.put("middleName", null);
    b.put("workEmail", email);
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

  private LeaveTypeDto createLeaveType(String code, String name, boolean paid, int maxPerYear)
      throws Exception {
    Map<String, Object> b = new HashMap<>();
    b.put("code", code);
    b.put("name", name);
    b.put("paid", paid);
    b.put("maxDaysPerYear", maxPerYear);
    MvcResult res =
        mvc.perform(
                post("/api/v1/leave-types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(b)))
            .andExpect(status().isCreated())
            .andReturn();
    return om.readValue(res.getResponse().getContentAsByteArray(), LeaveTypeDto.class);
  }
}
