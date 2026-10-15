package com.dwi.intranetdistribucion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IntranetDistribucionBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntranetDistribucionBackApplication.class, args);
		System.out.println("=================================================================");
		System.out.println(">>> BACKEND CORRIENDO EXITOSAMENTE EN HTTP://LOCALHOST:8080 <<<");
		System.out.println("=================================================================");
	}

}
