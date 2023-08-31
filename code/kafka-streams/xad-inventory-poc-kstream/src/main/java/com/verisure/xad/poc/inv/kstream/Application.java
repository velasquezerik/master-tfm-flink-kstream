package com.verisure.xad.poc.inv.kstream;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Application bootstrap class.
 *
 * @since 1.0.0
 * @author XAD-SolutionsArchitecture [SP.DG.IT.Architecture_Solutions
 */
@Slf4j
@SpringBootApplication
public class Application {

	protected Application() {
		LOGGER.info("Starting xad-inv-poc-kstream microservice");
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public ApplicationRunner runner(MeterRegistry registry) {
		return args -> {
			registry.getMeters().forEach(meter -> LOGGER.debug("" + meter.getId()));
		};
	}

	/**
	 * Timed aspect for custom timer metrics in arbitrary methods.
	 */
	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

}
