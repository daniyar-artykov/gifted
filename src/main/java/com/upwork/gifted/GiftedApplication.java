package com.upwork.gifted;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GiftedApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiftedApplication.class, args);
	}

}
