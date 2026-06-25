package com.club.rfid_access;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RfidAccessApplication {

	public static void main(String[] args) {
		SpringApplication.run(RfidAccessApplication.class, args);
	}
}
