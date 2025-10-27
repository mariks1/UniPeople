package com.khasanshin.employmentservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@FeignClient(name = "organization-service")
public interface OrgClient {

    @RequestMapping(method = RequestMethod.HEAD, value = "/api/v1/departments/{id}")
    ResponseEntity<Void> departmentExists(@PathVariable("id") UUID id);

    @RequestMapping(method = RequestMethod.HEAD, value = "/api/v1/positions/{id}")
    ResponseEntity<Void> positionExists(@PathVariable("id") UUID id);
}
