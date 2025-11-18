package com.khasanshin.employeeservice.feign;


import com.khasanshin.employeeservice.config.FeignAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "organization-service", path = "/api/v1/departments", configuration = FeignAuthConfig.class)
public interface OrgClient {

    @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
    ResponseEntity<Void> departmentExists(@PathVariable("id") UUID id);

    @DeleteMapping("/departments/head/by-employee/{employeeId}")
    void clearHeadByEmployee(@PathVariable("employeeId") UUID employeeId);
}
