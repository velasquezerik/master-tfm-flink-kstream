package com.verisure.xad.poc.inv.kstream.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.signal-excess-papi")
public class SignalExcessPapiConfig {

	private String appName;
	private Boolean enabled;
    private String inputTopic;
    private String outputTopic;
    private String signalExcessStateStoreName;
    private Duration signalExcessRetentionPeriod;
    private Duration signalExcessWindowSize;
    private int signalExcessThreshold;
    private String limitExecutionsStateStoreName;
    private Duration limitExecutionsRetentionPeriod;
    private Duration limitExecutionsWindowSize;
    private int limitExecutionsThreshold;
    
}

