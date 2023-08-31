package com.verisure.xad.poc.inv.kstream.topology;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.verisure.inv.avro.PanelEventAvroDTO;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(value = "app.signal-excess-dsl.enabled", havingValue = "true")
public class SignalExcessDSLTopology {

	private static final String INPUT_TOPIC = "xad.pocinv.panelevents.mc.streaming";
	private static final String OUTPUT_TOPIC = "xad.pocinv.paneleventsfiltered.mc.streaming";
	private static final int WINDOW_SIZE_MINUTES_MS = 5; 
	private static final int OCCURRENCES_THRESHOLD = 5;
	private static final Duration advanceSize = Duration.ofMinutes(1);

	@Autowired
	Map<String,String> schemaRegistryConfig;
	
	/**
	 * Count signals, using WINDOW_SIZE_MS tumbling windows;
	 * no need to specify explicit serdes because the resulting key and value types match our default serde settings
	 * @param streamsBuilder
	 * @return topology
	 */
	@Autowired
	public Topology signalExcessTopology(StreamsBuilder streamsBuilder) {
		LOGGER.info("Creating signal-excess-dsl-topology");
		final KStream<String, PanelEventAvroDTO> input = streamsBuilder.stream(INPUT_TOPIC,
				Consumed.with(Serdes.String(), getSourceValueSerde()));
		
		TimeWindows window = TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(WINDOW_SIZE_MINUTES_MS)).advanceBy(advanceSize);
//		SlidingWindows window = SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(WINDOW_SIZE_MINUTES_MS));

		KTable<Windowed<String>, Long> signalCounts = input
				.peek((key, value) -> LOGGER.info("[key={}] => signal received [{}]", key, value))
				.groupByKey()
				.windowedBy(window).count(Materialized.as("signal-counter"));

		signalCounts
				.filter((key, signalCounter) -> signalCounter != null && (signalCounter % OCCURRENCES_THRESHOLD == 0))
				.toStream((windowedKey, value) -> windowedKey.key())
				.filter((key, signalCounter) -> signalCounter != null)
				.groupByKey()
				.reduce((value1, value2) -> value1 > value2 ? value1 : value2)
				.toStream()
				.peek((key, value) -> LOGGER.info("[key={}] => selected key [{}] value [{}]", key, value))
				.to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));

		return streamsBuilder.build();
	}
	
	private SpecificAvroSerde<PanelEventAvroDTO> getSourceValueSerde() {
		// Set the schema
		SpecificAvroSerde<PanelEventAvroDTO> avroSerde = new SpecificAvroSerde<>();
		avroSerde.configure(schemaRegistryConfig, false);
		return avroSerde;
	}
	
}
