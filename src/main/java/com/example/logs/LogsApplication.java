package com.example.logs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogsApplication {
	// Logs watcher service 09/03/2025
	public static void main(String[] args) {
		SpringApplication.run(LogsApplication.class, args);
	}

}
