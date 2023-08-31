package com.verisure.xad.poc.inv.kstream.topology;

import java.util.Map;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.xad.poc.inv.kstream.config.AppConfig;
import com.verisure.xad.poc.inv.kstream.config.SignalExcessPapiConfig;
import com.verisure.xad.poc.inv.kstream.supplier.SignalExcessLimitExecutionsProcessorSupplier;
import com.verisure.xad.poc.inv.kstream.supplier.SignalExcessProcessorSupplier;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(value = "app.signal-excess-papi.enabled", havingValue = "true")
public class SignalExcessPAPITopology {
	
	@Autowired
	private SignalExcessPapiConfig config;
	
	@Autowired
	private AppConfig appConfig;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private Map<String,String> schemaRegistryConfig;

	/**
	 * Read events (signals) from an input topic. Count those signals grouped by key within a time window, 
	 * if the counter reach a threshold forwards an event to the next processor. 
	 * The second processor count executions of that event within another time window, 
	 * if the counter reach another threshold, the processor publishes an event to an output topic.
	 * Implemented with the Kafka Streams Processor API (PAPI)
	 * 
	 * @param streamsBuilder
	 * @return topology
	 */
	@Autowired
	public void signalExcessTopology(StreamsBuilder streamsBuilder) {
		LOGGER.info("Creating signal-excess-papi-topology");
		
		SignalExcessProcessorSupplier signalExcessSupplier = beanFactory.getBean(SignalExcessProcessorSupplier.class, config.getSignalExcessStateStoreName(), config.getSignalExcessThreshold(), config.getSignalExcessWindowSize().toMillis());
		SignalExcessLimitExecutionsProcessorSupplier limitExecutionsSupplier = beanFactory.getBean(SignalExcessLimitExecutionsProcessorSupplier.class, config.getLimitExecutionsStateStoreName(), config.getLimitExecutionsThreshold(), config.getLimitExecutionsWindowSize().toMillis());

		Topology topology = streamsBuilder.build();
		topology
			.addSource("signal-excess-source", Serdes.String().deserializer(), getSourceValueSerde(), config.getInputTopic())
			.addProcessor("signal-excess-processor", signalExcessSupplier, "signal-excess-source")	
			.addStateStore(getSignalExcessStoreBuilder(), "signal-excess-processor")
			.addProcessor("limit-executions-processor", limitExecutionsSupplier, "signal-excess-processor")
			.addStateStore(getLimitExecutionsStoreBuilder(), "limit-executions-processor")
			.addSink("signal-excess-sink", config.getOutputTopic(), Serdes.String().serializer(), Serdes.String().serializer(), "limit-executions-processor");
	}
	
	private SpecificAvroDeserializer<PanelEventAvroDTO> getSourceValueSerde() {
		SpecificAvroDeserializer<PanelEventAvroDTO> avroSerde = new SpecificAvroDeserializer<>();
		avroSerde.configure(schemaRegistryConfig, false);
		return avroSerde;
	}
	
    public StoreBuilder<?> getSignalExcessStoreBuilder() {
		final StoreBuilder<WindowStore<String, Long>> storeBuilder = Stores
				.windowStoreBuilder(Stores.persistentWindowStore(config.getSignalExcessStateStoreName(), config.getSignalExcessRetentionPeriod(), config.getSignalExcessWindowSize(), false),
						Serdes.String(), Serdes.Long()).withLoggingEnabled(appConfig.getChangelogConfig());
        return storeBuilder;
    }
    
    public StoreBuilder<?> getLimitExecutionsStoreBuilder() {
		final StoreBuilder<WindowStore<String, Long>> storeBuilder = Stores
				.windowStoreBuilder(Stores.persistentWindowStore(config.getLimitExecutionsStateStoreName(), config.getLimitExecutionsRetentionPeriod(), config.getLimitExecutionsWindowSize(), false),
						Serdes.String(), Serdes.Long()).withLoggingEnabled(appConfig.getChangelogConfig());
        return storeBuilder;
    }

}
