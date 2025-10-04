package org.springframework.samples.petclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "org.springframework.samples.petclinic.model") // Указывает, где искать @Entity
@EnableJpaRepositories(basePackages = "org.springframework.samples.petclinic.repository") // Указывает, где искать @Repository интерфейсы (Spring Data JPA)
public class PetClinicApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(PetClinicApplication.class, args);
	}
}
