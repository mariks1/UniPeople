package com.khasanshin.organizationservice.feign;

import com.khasanshin.organizationservice.config.FeignAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@FeignClient(name = "employee-service", path = "/api/v1/employees", configuration = FeignAuthConfig.class)
public interface EmployeeClient {

    @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
    ResponseEntity<Void> exists(@PathVariable("id") UUID id);

}
