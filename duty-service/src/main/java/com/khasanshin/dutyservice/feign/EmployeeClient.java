package com.khasanshin.dutyservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "employee-service", path = "/api/v1/employees")
public interface EmployeeClient {
    @GetMapping("/{id}")
    ResponseEntity<Void> employeeExists(@PathVariable("id") UUID id);
}
