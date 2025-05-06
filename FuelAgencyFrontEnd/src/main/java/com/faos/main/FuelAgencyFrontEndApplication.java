package com.faos.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.faos.*"})
@EntityScan("com.faos.model")
@SpringBootApplication
public class FuelAgencyFrontEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(FuelAgencyFrontEndApplication.class, args);
		System.out.println("FRONTEND RUNNING ON PORT: 1000");
	}

}