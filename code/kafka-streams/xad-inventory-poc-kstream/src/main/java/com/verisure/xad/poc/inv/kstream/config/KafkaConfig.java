package com.verisure.xad.poc.inv.kstream.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableKafka
@EnableKafkaStreams
@Slf4j
public class KafkaConfig {
	
	private String INTERNAL_LEAVEL_GROUP_ON_CLOSE_KEY = "internal.leave.group.on.close";
	
	@Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
	public KafkaStreamsConfiguration kafkaStreamsConfig(KafkaProperties kafkaProperties, AppConfig appConfig) {

		Map<String, Object> config = new HashMap<>();
		// inject SSL related properties
		config.putAll(kafkaProperties.getSsl().buildProperties());
		config.putAll(kafkaProperties.getProperties());

		kafkaProperties.getStreams().getCleanup().setOnShutdown(false);
		kafkaProperties.getStreams().getCleanup().setOnStartup(false);

		// Stream config 
		config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
		config.put(StreamsConfig.APPLICATION_ID_CONFIG, appConfig.getStreamApplicationName());
		config.put(StreamsConfig.STATE_DIR_CONFIG, kafkaProperties.getStreams().getStateDir());
		config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		// Set the commit interval to 500ms so that any changes are flushed frequently.
		// The low latency would be important for this use case.
		config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500);
		config.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
				LogAndContinueExceptionHandler.class);
		config.put(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, StreamsConfig.OPTIMIZE);
		config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 1 * 1024 * 1024L);
		
		config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, kafkaProperties.getStreams().getProperties().get(StreamsConfig.NUM_STREAM_THREADS_CONFIG));		
		config.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, kafkaProperties.getStreams().getProperties().get(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG));
		
		//Consumer config
		config.put(INTERNAL_LEAVEL_GROUP_ON_CLOSE_KEY, kafkaProperties.getConsumer().getProperties().get(INTERNAL_LEAVEL_GROUP_ON_CLOSE_KEY));
		config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaProperties.getConsumer().getProperties().get(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG));
		config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaProperties.getConsumer().getProperties().get(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG));		
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		return new KafkaStreamsConfiguration(config);
	}
	
	@Bean(name = "SchemaRegistryConfig")
	public static Map<String, String> getSchemaRegistryConfig(KafkaProperties kafkaProperties) {
		Map<String, String> config = new HashMap<>();
		config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaProperties.getProperties().get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
		return config;
	}
	
	
	@Bean
	public StreamsBuilderFactoryBeanConfigurer configure() {
		return factory -> factory.setStateListener(
				(newState, oldState) -> LOGGER.info("State transition from " + oldState + " to " + newState));
	}
}
