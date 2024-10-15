package com.CodeEvalCrew.AutoScore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@ComponentScan(basePackages = "com.CodeEvalCrew.AutoScore")
@EnableJpaRepositories(basePackages = "com.CodeEvalCrew.AutoScore.repositories")
public class AutoScoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoScoreApplication.class, args);
	}

}
