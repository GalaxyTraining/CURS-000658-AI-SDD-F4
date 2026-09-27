package pe.edu.galaxy.training.java.sdd.catalog.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composition root for the current time (architecture.md §5); tests replace it with a fixed clock. */
@Configuration
public class ClockConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
