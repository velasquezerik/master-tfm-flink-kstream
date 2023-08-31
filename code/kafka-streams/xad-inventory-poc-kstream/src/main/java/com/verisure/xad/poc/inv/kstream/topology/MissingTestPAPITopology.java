package com.verisure.xad.poc.inv.kstream.topology;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.common.serialization.Serde;
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

import com.verisure.inv.avro.MissingTestEventAvro;
import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.xad.poc.inv.kstream.supplier.MissingTestProcessorSupplier;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(value = "app.missing-test-window-papi.enabled", havingValue = "true")
public class MissingTestPAPITopology {

	@Autowired
	private BeanFactory beanFactory;

	private static final String INPUT_TOPIC = "xad.pocinv.panelevents.mc.streaming";
	private static final String OUTPUT_TOPIC = "xad.pocinv.panelmissingtests.mc.streaming";

	private static final Serde<String> STRING_SERDE = Serdes.String();
	
	private String stateStoreName = "missing-test-window-store";
	private Duration retentionPeriod = Duration.ofDays(2);
	private Duration windowSize = Duration.ofDays(1);

	@Autowired
	Map<String,String> schemaRegistryConfig;

	/**
	 * Count signals, using WINDOW_SIZE_MS tumbling windows; no need to specify
	 * explicit serdes because the resulting key and value types match our default
	 * serde settings
	 * 
	 * @param streamsBuilder
	 * @return topology
	 */
	@Autowired
	public void missingTestTopology(StreamsBuilder streamsBuilder) {
		LOGGER.info("Creating missing-test-window-topology");
		MissingTestProcessorSupplier processorSupplier = beanFactory.getBean(MissingTestProcessorSupplier.class, stateStoreName, windowSize.toMillis());

		Topology topology = streamsBuilder.build();
		topology.addSource("missing-test-source", STRING_SERDE.deserializer(), getSourceValueSerde(), INPUT_TOPIC)
				.addProcessor("missing-test-processor", processorSupplier, "missing-test-source")
				.addStateStore(getStoreBuilder(), "missing-test-processor")
				.addSink("missing-test-sink", OUTPUT_TOPIC, STRING_SERDE.serializer(), getSinkValueSerde().serializer(), "missing-test-processor");
	}
	
	private SpecificAvroDeserializer<PanelEventAvroDTO> getSourceValueSerde() {
		// Set the schema
		SpecificAvroDeserializer<PanelEventAvroDTO> avroSerde = new SpecificAvroDeserializer<>();
		avroSerde.configure(schemaRegistryConfig, false);
		return avroSerde;
	}
	
	private SpecificAvroSerde<MissingTestEventAvro> getSinkValueSerde() {
		// Set the schema
		SpecificAvroSerde<MissingTestEventAvro> avroSerde = new SpecificAvroSerde<>();
		avroSerde.configure(schemaRegistryConfig, false);
		return avroSerde;
	}
	
    public StoreBuilder<?> getStoreBuilder() {
		final StoreBuilder<WindowStore<String, MissingTestEventAvro>> storeBuilder = Stores
				.windowStoreBuilder(Stores.persistentWindowStore(stateStoreName, retentionPeriod, windowSize, false),
						Serdes.String(), getStoreValueSerde());
        return storeBuilder;
    }
    
	private SpecificAvroSerde<MissingTestEventAvro> getStoreValueSerde() {
		// Set the schema
		SpecificAvroSerde<MissingTestEventAvro> avroSerde = new SpecificAvroSerde<>();
		avroSerde.configure(schemaRegistryConfig, false);
		return avroSerde;
	}


}
