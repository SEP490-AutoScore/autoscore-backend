package com.CodeEvalCrew.AutoScore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.CodeEvalCrew.AutoScore")
public class AutoScoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoScoreApplication.class, args);
	}

}
