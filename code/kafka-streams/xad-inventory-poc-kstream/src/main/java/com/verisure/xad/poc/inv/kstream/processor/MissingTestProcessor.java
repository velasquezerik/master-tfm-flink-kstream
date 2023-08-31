package com.verisure.xad.poc.inv.kstream.processor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.verisure.inv.avro.MissingTestEventAvro;
import com.verisure.inv.avro.PanelEvent;
import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.inv.avro.Parameters;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(value = "prototype")
public class MissingTestProcessor implements Processor<String, PanelEventAvroDTO, String, MissingTestEventAvro>{
	
	private ProcessorContext<String, MissingTestEventAvro> context;
	private WindowStore<String, MissingTestEventAvro> stateStore;
	private String stateStoreName;
	private long windowSize;
	
    //time start timestamp (inclusive) of the window to search records
    long windowStartTime = 0L;
    
    // create Clock Object
    Clock clock = Clock.systemDefaultZone();
    
    public MissingTestProcessor(String stateStoreName, 	long windowSize) {
    	this.stateStoreName = stateStoreName;
    	this.windowSize = windowSize;
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public void init(ProcessorContext<String, MissingTestEventAvro> context) {
		this.context = context;
	    this.stateStore = (WindowStore<String, MissingTestEventAvro>) context.getStateStore(stateStoreName);
		this.context.schedule(Duration.ofSeconds(10), PunctuationType.WALL_CLOCK_TIME, this::checkMissingTest);
	    //This is just for debug purposes
//		this.context.schedule(Duration.ofSeconds(10), PunctuationType.WALL_CLOCK_TIME, this::countStateStoreElements);
	}
       
    @Override
    @Timed(value = "kafka.stream.missingtestprocessor.process", description = "Time spent processing a record", extraTags = {"scenario","process"})
	public void process(Record<String, PanelEventAvroDTO> record) {
		if(record.key() != null) {
			LOGGER.debug("[key={}] => signal received", record.key());
	        PanelEvent event = record.value().getPanelEvent();
	        String countryAndInstallation = event.getSdKey().getIdCountry() + "_" + record.value().getPanelEvent().getSdKey().getIdInstallation();
	        
			// Search records from the very beginning to now
			try(final WindowStoreIterator<MissingTestEventAvro> iter = stateStore.fetch(countryAndInstallation, windowStartTime,  Instant.now(clock).toEpochMilli())) {
				// Iterate over all the records in the window and delete it (set NULL value)
				while (iter.hasNext()) {
					KeyValue<Long, MissingTestEventAvro> keyValue = iter.next();
				    stateStore.put(countryAndInstallation, null, keyValue.key);
				}
			}
			
			//TODO Review this mapping. The process can be optimized if we only save the key with the timestamp. 
			MissingTestEventAvro value = mapPanelEventToStateStoreValue(event);
			
			//Put the record in the stateStore with the event timestamp value
			stateStore.put(countryAndInstallation, value, record.value().getPanelEvent().getSdKey().getTimestampEvent());
		} else {
			LOGGER.warn("Message with key NULL ignored");
		}
	}

    @Timed(value = "kafka.stream.missingtestprocessor.check", description = "Time spent checking missing test", extraTags = {"scenario","checkMissingTest"})
    public void checkMissingTest(long now) {
    	int expiredRecords = 0;
    	long expirationTimestamp = now - windowSize;
    	// Select expired records: those whose startTime is before (now - grace period)
		try(KeyValueIterator<Windowed<String>, MissingTestEventAvro> iter = stateStore.fetchAll(windowStartTime, expirationTimestamp)){
			while (iter.hasNext()) {				
				expiredRecords++;
				final KeyValue<Windowed<String>, MissingTestEventAvro> entry = iter.next();				
				LOGGER.debug("No event received for key {} in the last {} milliseconds.", entry.key, windowSize);
				Record<String, MissingTestEventAvro> record = new Record<String, MissingTestEventAvro>(entry.key.key(), entry.value, now);
				context.forward(record);
				stateStore.put(entry.key.key(), null, entry.key.window().start());
				stateStore.put(entry.key.key(), entry.value, now);
			}
		}
		LOGGER.info("Number of records checked for missing test {} from the beginning to {}", expiredRecords, Instant.ofEpochMilli(expirationTimestamp));
	}
    
    @Timed(value = "kafka.stream.missingtestprocessor.count", description = "Time spent counting elements in stateStore", extraTags = {"scenario","countStateStoreElements"})
    public void countStateStoreElements(long now, WindowStore<String, MissingTestEventAvro> stateStore) {
    	long records = 0;
    	// Select expired records: those whose startTime is before (now - grace period)
		try(KeyValueIterator<Windowed<String>, MissingTestEventAvro> iter = stateStore.fetchAll(windowStartTime, now)){
			while(iter.hasNext()) {
				iter.next();
				records++;
			}
		}
		LOGGER.info("Number of records {} from the beginning to now", records);
	}
    

	
	private MissingTestEventAvro mapPanelEventToStateStoreValue(PanelEvent panelEvent) {
		Parameters parameters = Parameters
				.newBuilder()
				//TODO este parametro no esta claro de donde mapearlo
				.setChannel(panelEvent.getChannelComm())
				.setCountry(panelEvent.getSdKey().getIdCountry())
				.setEventType(panelEvent.getDevType())
				.setInstallationId(panelEvent.getSdKey().getIdInstallation() + "")
				.setMedium(panelEvent.getChannelComm())
				.setTimeInSeconds("90000")
				//TODO revisar si esto se saca de la cache
				.setVersion("1.01.01")
				.build();
		MissingTestEventAvro value = MissingTestEventAvro
				.newBuilder()
				.setFlowName("WRMMCL")
				.setParameters(parameters)
				.build();
		return value;
	}
	
	@Override
	public void close() {
		// close any resources managed by this processor
        // Note: Do not close any StateStores as these are managed by the library
	}
	
}
