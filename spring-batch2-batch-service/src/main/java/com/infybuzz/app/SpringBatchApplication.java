package com.infybuzz.app;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan({"com.infybuzz.config", 
	"com.infybuzz.service",
	"com.infybuzz.reader",
	"com.infybuzz.processor",
	"com.infybuzz.writer",
	"com.infybuzz.listener",
	"com.infybuzz.controller"})
@EnableAsync
@EnableBatchProcessing
@EnableJpaRepositories(entityManagerFactoryRef = "mysqlEntityManagerFactory")
public class SpringBatchApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringBatchApplication.class, args);
	}
}
