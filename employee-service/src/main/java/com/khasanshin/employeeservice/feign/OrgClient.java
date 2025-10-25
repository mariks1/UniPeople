package com.khasanshin.employeeservice.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "organization-service", path = "/api/v1")
public interface OrgClient {

    @GetMapping("/departments/{id}")
    ResponseEntity<Void> departmentExists(@PathVariable("id") UUID id);

    @DeleteMapping("/departments/head/by-employee/{employeeId}")
    void clearHeadByEmployee(@PathVariable("employeeId") UUID employeeId);
}
