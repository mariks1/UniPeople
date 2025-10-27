package com.khasanshin.employmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EmploymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmploymentServiceApplication.class, args);
    }

}
