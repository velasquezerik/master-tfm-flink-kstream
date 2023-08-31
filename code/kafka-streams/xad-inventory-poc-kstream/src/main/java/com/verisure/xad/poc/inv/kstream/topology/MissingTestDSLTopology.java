package com.verisure.xad.poc.inv.kstream.topology;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.xad.poc.inv.kstream.mapper.PanelEventAvroMapper;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(value = "app.missing-test-dsl.enabled", havingValue = "true")
public class MissingTestDSLTopology {

	private static final String INPUT_TOPIC = "xad.pocinv.panelevents.mc.streaming";
	private static final String OUTPUT_TOPIC = "xad.pocinv.panelmissingtests.mc.streaming";
	private static final int WINDOW_SIZE_MINUTES = 1;
	private static final int GRACE_PRERIOD_SECONDS = 10;

	@Autowired
	Map<String, String> schemaRegistryConfig;

	/**
	 * Count signals, using WINDOW_SIZE_MS tumbling windows; no need to specify
	 * explicit serdes because the resulting key and value types match our default
	 * serde settings
	 * 
	 * @param streamsBuilder
	 * @return topology
	 */
	@Autowired
	public Topology missingTestTopology(StreamsBuilder streamsBuilder) {
		LOGGER.info("Creating missing-test-dsl-topology");
		final KStream<String, PanelEventAvroDTO> input = streamsBuilder.stream(INPUT_TOPIC,
				Consumed.with(Serdes.String(), getSourceValueSerde()));

		KStream<String, String> result = input
			.peek((key, value) -> LOGGER.info("[key={}] => signal received [{}]", key, value))
			.groupByKey()
			.windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(WINDOW_SIZE_MINUTES),Duration.ofSeconds(GRACE_PRERIOD_SECONDS)))
			.count()
			.suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded()))
			.filter((windowedUserId, count) -> count < 1)
			.toStream()
			.map((key, value) -> KeyValue.pair(key.toString(), value.toString()));
		
		//Write the the missing test topic
		result
			.peek((key, value) -> LOGGER.info("[key={}] => sending missing test count events:[{}]", key, value))
			.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
		
		//Write again to the input_topic to restart the counter
		result
			.map(new PanelEventAvroMapper())
			.to(INPUT_TOPIC, Produced.with(Serdes.String(), getSourceValueSerde()));

		return streamsBuilder.build();
	}

	private SpecificAvroSerde<PanelEventAvroDTO> getSourceValueSerde() {
		// Set the schema
		SpecificAvroSerde<PanelEventAvroDTO> avroSerde = new SpecificAvroSerde<>();
		avroSerde.configure(schemaRegistryConfig, false);
		return avroSerde;
	}

}
