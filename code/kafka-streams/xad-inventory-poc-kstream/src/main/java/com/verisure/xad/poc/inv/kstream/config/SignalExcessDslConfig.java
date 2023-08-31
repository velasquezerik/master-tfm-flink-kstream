package com.verisure.xad.poc.inv.kstream.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.signal-excess-dsl")
public class SignalExcessDslConfig {

	private String appName;
	private Boolean enabled;
    
}

