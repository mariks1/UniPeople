package com.khasanshin.dutyservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@FeignClient(name = "organization-service", path = "/api/v1/departments")
public interface OrgClient {

    @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
    ResponseEntity<Void> departmentExists(@PathVariable("id") UUID id);
}

