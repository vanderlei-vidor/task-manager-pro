package com.example.demo;

import com.example.demo.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da aplicação Task Manager Pro.
 *
 * O seed de dados de desenvolvimento (admin de teste) foi movido para
 * {@link com.example.demo.config.DataInitializer}, isolado em @Profile("dev"),
 * de forma que NUNCA roda em produção.
 */
@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableScheduling
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
