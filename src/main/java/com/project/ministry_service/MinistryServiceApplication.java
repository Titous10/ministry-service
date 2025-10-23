package com.project.ministry_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.project.ministry_service.client")
public class MinistryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinistryServiceApplication.class, args);
	}

}
