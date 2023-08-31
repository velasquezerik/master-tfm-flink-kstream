package com.verisure.xad.poc.inv.kstream.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import co.elastic.apm.attach.ElasticApmAttacher;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Configuration
@ConfigurationProperties(prefix = "elastic.apm")
@ConditionalOnProperty(value = "elastic.apm.enabled", havingValue = "true")
public class ElasticApmConfig {
    
    private static final String SERVER_URL_KEY = "server_url";
    private String serverUrl;
    
    private static final String SERVICE_NAME_KEY = "service_name";
    private String serviceName;
    
    private static final String ENVIRONMENT_KEY = "environment";
    private String environment;
    
    private static final String APPLICATION_PACKAGES_KEY = "application_packages";
    private String applicationPackages;
    
    private static final String LOG_LEVEL_KEY = "log_level";
    private String logLevel;
    
    private static final String GLOBAL_LABELS_KEY = "global_labels";
    private String globalLabels;
    
    private static final String TRANSACTION_SAMPLE_RATE_KEY = "transaction_sample_rate";
    private String transactionSampleRate = "transaction_sample_rate";
    
    private static final String SECRET_TOKEN_KEY = "secret_token";
    private String secretToken;
    
    private static final String API_KEY_KEY = "api_key";
    private String apiKey;
    
    private static final String ENABLE_INSTRUMENTATIONS_KEY = "enable_instrumentations";
    private String enableInstrumentations;
    
    private static final String ENABLE_EXPERIMENTAL_INSTRUMENTATIONS_KEY = "enable_experimental_instrumentations";
    private String enableExperimentalInstrumentations;
    
    private static final String METRICS_INTERVAL_KEY = "metrics_interval";
    private String metricsInterval;
    
    private static final String METRIC_SET_KEY = "metric_set_limit";
    private String metric_set_limit;
    
    private static final String METRICS_ENABLED = "metrics_enabled";
    private String metrics_enabled;

    private static final String ENABLE_BYTECODE_VISIBILITY = "enable_bytecode_visibility";
    private String enable_bytecode_visibility;
    
    @Autowired
    Environment executionEnvironment; 
    
    @PostConstruct
    public void init() {
        
        Map<String, String> apmProps = new HashMap<>(15);
        apmProps.put(SERVER_URL_KEY, serverUrl);
        apmProps.put(SERVICE_NAME_KEY, serviceName);
        apmProps.put(ENVIRONMENT_KEY, environment);
        apmProps.put(APPLICATION_PACKAGES_KEY, applicationPackages);
        apmProps.put(LOG_LEVEL_KEY, logLevel);
        apmProps.put(GLOBAL_LABELS_KEY, new StringBuilder(globalLabels).append(",instance=").append(getIstanceId()).toString());
        apmProps.put(TRANSACTION_SAMPLE_RATE_KEY, transactionSampleRate);
        apmProps.put(ENABLE_EXPERIMENTAL_INSTRUMENTATIONS_KEY, enableExperimentalInstrumentations);
        apmProps.put(METRICS_INTERVAL_KEY, metricsInterval);
        apmProps.put(METRIC_SET_KEY, metric_set_limit);
        apmProps.put(METRICS_ENABLED, metrics_enabled);
        apmProps.put(ENABLE_BYTECODE_VISIBILITY, enable_bytecode_visibility);
        
        if(enableInstrumentations != null) {
        	apmProps.put(ENABLE_INSTRUMENTATIONS_KEY, enableInstrumentations);
        }
        if(apiKey != null) {
        	apmProps.put(API_KEY_KEY, apiKey);
        }
        if(secretToken != null) {
        	apmProps.put(SECRET_TOKEN_KEY, secretToken);
        }

        ElasticApmAttacher.attach(apmProps);
    }
       
	private String getIstanceId() {
		String instance = null;
		try {
			instance = System.getenv("POD_NAME");
			if(instance == null) {
				InetAddress ip = InetAddress.getLocalHost();
				instance = ip.getHostAddress().toString();
			}
			LOGGER.info("Instance name : " + instance);
		} catch (UnknownHostException e) {
			LOGGER.warn("Instance name not added to metric configuration due to an error {}", e.getMessage());
		}
		return instance;
	}
}

