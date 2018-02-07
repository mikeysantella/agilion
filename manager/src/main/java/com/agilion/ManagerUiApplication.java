package com.agilion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class ManagerUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManagerUiApplication.class, args);
	}
}
