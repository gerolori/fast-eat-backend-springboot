package com.gerolori.fasteat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FasteatApplication {

	public static void main(String[] args) {
		SpringApplication.run(FasteatApplication.class, args);
	}

}
