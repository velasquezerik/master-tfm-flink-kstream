package com.verisure.xad.poc.inv.kstream;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApplicationEventListeners {

	@Autowired
	private StreamsBuilderFactoryBean factoryBean;

	@EventListener
	public void handleContextStart(ApplicationStartedEvent cse) {
		LOGGER.info(factoryBean.getTopology().describe().toString());

		KafkaStreams streams = factoryBean.getKafkaStreams();

//		streams.setUncaughtExceptionHandler(new LogAndReplaceTheadHandler());

		// Add shutdown hook to stop the Kafka Streams threads.
		// You can optionally provide a timeout to `close`.
		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}

	public class LogAndReplaceTheadHandler implements StreamsUncaughtExceptionHandler {
		public StreamThreadExceptionResponse handle(Throwable throwable) {
			LOGGER.warn("Uncaught Exception, " + "exception: {}", throwable);
			return StreamThreadExceptionResponse.REPLACE_THREAD;
		}
	}

}
