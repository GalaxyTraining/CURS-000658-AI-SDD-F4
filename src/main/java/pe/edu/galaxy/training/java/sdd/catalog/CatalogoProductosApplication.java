package pe.edu.galaxy.training.java.sdd.catalog;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CatalogoProductosApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatalogoProductosApplication.class, args);
	}

	/** Current time for the services (architecture.md §5); tests replace it with a fixed clock. */
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}
