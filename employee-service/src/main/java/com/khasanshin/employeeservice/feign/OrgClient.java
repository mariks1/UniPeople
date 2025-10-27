package com.khasanshin.employeeservice.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "organization-service", path = "/api/v1/departments")
public interface OrgClient {

    @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
    void departmentExists(@PathVariable("id") UUID id);

    @DeleteMapping("/departments/head/by-employee/{employeeId}") // TODO
    void clearHeadByEmployee(@PathVariable("employeeId") UUID employeeId);
}
