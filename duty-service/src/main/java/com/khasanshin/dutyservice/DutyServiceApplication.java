package com.khasanshin.dutyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.khasanshin.dutyservice.feign")
@SpringBootApplication
public class DutyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DutyServiceApplication.class, args);
	}

}
