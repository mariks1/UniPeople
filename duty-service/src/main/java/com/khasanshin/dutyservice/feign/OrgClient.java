package com.khasanshin.dutyservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "organization-service", path = "/api/v1")
public interface OrgClient {
    @GetMapping("/departments/{id}")
    ResponseEntity<Void> departmentExists(@PathVariable("id") UUID id);
}

