package com.verisure.xad.poc.inv.kstream.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.missing-test-dsl")
public class MissingTestDslConfig {

	private String appName;
	private Boolean enabled;
    
}

