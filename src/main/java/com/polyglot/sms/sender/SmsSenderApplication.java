package com.polyglot.sms.sender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class SmsSenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmsSenderApplication.class, args);
		log.info("SmsSenderApplication started successfully on port 8080");
	}

}
