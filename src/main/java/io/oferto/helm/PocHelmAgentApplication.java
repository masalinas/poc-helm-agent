package io.oferto.helm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PocHelmAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocHelmAgentApplication.class, args);
	}

}
