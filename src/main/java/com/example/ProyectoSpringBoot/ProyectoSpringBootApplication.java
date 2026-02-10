package com.example.ProyectoSpringBoot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Habilita tareas programadas para facturación automática
public class ProyectoSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoSpringBootApplication.class, args);
	}

}
