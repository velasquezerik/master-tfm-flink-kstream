package com.verisure.xad.poc.inv.kstream.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

	@Autowired
	private MissingTestDslConfig missingTestDslConfig;
	
	@Autowired
	private MissingTestPapiConfig missingTestPapiConfig;

	@Autowired
	private SignalExcessDslConfig signalExcessDslConfig;

	@Autowired
	private SignalExcessPapiConfig signalExcessPapiConfig;
	
	private Map<String, String> changelogConfig;

    	
    public String getStreamApplicationName() {
    	String appName = "no-application-name-configured-group";
    	if(missingTestDslConfig.getEnabled()) {
    		appName = missingTestDslConfig.getAppName();
    	} else if (missingTestPapiConfig.getEnabled()) {
    		appName = missingTestPapiConfig.getAppName();
    	} else if (signalExcessDslConfig.getEnabled()) {
    		appName = signalExcessDslConfig.getAppName();
    	} else if (signalExcessPapiConfig.getEnabled()){
    		appName = signalExcessPapiConfig.getAppName();
    	}
    	return appName;
    }
    
}

