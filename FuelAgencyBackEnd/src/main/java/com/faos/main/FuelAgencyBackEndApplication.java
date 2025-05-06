package com.faos.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = {"com.faos.*"})
@EnableJpaRepositories("com.faos.repositories")
@EntityScan("com.faos.model")
@SpringBootApplication
public class FuelAgencyBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(FuelAgencyBackEndApplication.class, args);
		System.out.println("BACKEND RUNNING");
	}

}
