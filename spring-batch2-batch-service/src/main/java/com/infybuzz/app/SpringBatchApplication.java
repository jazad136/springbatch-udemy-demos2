package com.infybuzz.app;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({"com.infybuzz.config", 
	"com.infybuzz.service",
	"com.infybuzz.reader",
	"com.infybuzz.processor",
	"com.infybuzz.writer",
	"com.infybuzz.listener",
	"com.infybuzz.controller",
	"com.infybuzz.repo"})
//@EnableAsync
@EnableBatchProcessing
@EnableJpaRepositories
//@EnableAutoConfiguration(exclude = {HibernateJpaAutoConfiguration.class})
public class SpringBatchApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringBatchApplication.class, args);
	}
}
