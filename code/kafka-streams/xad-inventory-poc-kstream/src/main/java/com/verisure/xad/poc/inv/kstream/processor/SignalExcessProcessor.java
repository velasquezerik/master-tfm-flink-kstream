package com.verisure.xad.poc.inv.kstream.processor;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.verisure.inv.avro.PanelEventAvroDTO;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of a Kafka Processor that save every received record in a window state store with the current timestamp.
 * Subsequently, counts the number of records saved in the state store within the configured window, if it reaches the threshold
 * forward the a new record with the key (installation number) and the current timestamp.
 * 
 * It is important to take into account the that the default commit.interval is 100 milliseconds,
 * so if we expect that we are going to receive more than one signal for the same installation (stateStore key)
 * within that interval, it will be necessary to reduce the commit.interval period. Alternatively, you should control 
 * it manually by using context.commit(). In this case, we strongly recommend to review the impact on the performance. 
 *
 *
 */
@Slf4j
@Component
@Scope(value = "prototype")
public class SignalExcessProcessor implements Processor<String, PanelEventAvroDTO, String, String>{
	
	private ProcessorContext<String, String> context;
	private WindowStore<String, Long> stateStore;
    private String stateStoreName;
    private int threshold;
    private long windowSize;
	
    
    public SignalExcessProcessor(String stateStoreName, int threshold, long windowSize) {
    	this.stateStoreName = stateStoreName;
    	this.threshold = threshold;
    	this.windowSize = windowSize;
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(ProcessorContext<String, String> context) {
		this.context = context;
	    this.stateStore = (WindowStore<String, Long>) context.getStateStore(stateStoreName);
	}
	
    @Override
    @Timed(value = "kafka.stream.signalexcessprocessor", description = "Time spent processing a record", extraTags = {"scenario","process"})
	public void process(Record<String, PanelEventAvroDTO> record) {
    	String key = record.key();
		long now = System.currentTimeMillis();
		long startTime = now - windowSize;
		//update the record
		stateStore.put(key, 0L, now);
		int count = countEventsInWindowByKeyUntilThreshold(key, startTime, now);
		checkAndForwardSignalExcessEvent(key, count, startTime, now); 
	}
        
    private int countEventsInWindowByKeyUntilThreshold(String key, long startTime, long endTime) {
        int count = 0;
        try (WindowStoreIterator<Long> iter = stateStore.fetch(key, startTime, endTime)) {
            for (; count <= threshold && iter.hasNext(); count++) {
                iter.next();
            }
        }
        return count;
    }
    
    private void checkAndForwardSignalExcessEvent(String key, int count, long startTime, long endTime) {
		boolean limitReached = count > threshold;
		LOGGER.debug("Signal excess {} reached for key {} with total count {}", limitReached ? "" : "NOT", key, count);
		// Signal excess reached, forward the signal excess event to the output topic
		if (count >= threshold) {	
			context.forward(new Record<String, String>(key, count + "", endTime));
			removeEventsInWindowByKey(key, startTime, endTime);
		}
    }
    
    private void removeEventsInWindowByKey(String key, long startTime, long endTime) {
		// Search the record by key from the beginning to now remove the events (put with null value and key + timestamp in the window) 
		try(final WindowStoreIterator<Long> iter = stateStore.fetch(key, startTime, endTime)) {
			while (iter.hasNext()) {
				stateStore.put(key, null, iter.next().key);
			}
		}
    }


	@Override
	public void close() {
		// close any resources managed by this processor
        // Note: Do not close any StateStores as these are managed by the library
	}
}
