package com.verisure.xad.poc.inv.kstream.processor;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of a Kafka Processor that save every received record in a window state store with the current timestamp.
 * Subsequently, counts the number of records saved in the state store within the configured window, if it reaches the threshold
 * forward the a new record with the key (installation number) and the value=true, otherwise forward the record with value=false.
 *  
 *
 *
 */
@Slf4j
@Component
@Scope(value = "prototype")
public class SignalExcessLimitExecutionsProcessor implements Processor<String, Integer, String, String>{
	
	private ProcessorContext<String, String> context;
	private WindowStore<String, Long> stateStore;
    private String stateStoreName;
    private int threshold;
    private long windowSize;
	
    public SignalExcessLimitExecutionsProcessor(String stateStoreName, int threshold, long windowSize) {
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
    @Timed(value = "kafka.stream.limitexecutions", description = "Time spent processing a record in milliseconds", extraTags = {"scenario","process"})
	public void process(Record<String, Integer> record) {
    	String key = record.key();
		long now = System.currentTimeMillis();
		long startTime = now - windowSize;
		//insert the record in the stateStore
		stateStore.put(key, 0L, now);
		int count = countEventsInWindowByKeyUntilThreshold(key, startTime, now);
		forwardExecutionsEvent(key, count, startTime, now);
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
    
    private void forwardExecutionsEvent(String key, int count, long startTime, long endtime) {
		boolean limitReached = count >= threshold;
		LOGGER.info("Executions limit {} reached for key {} with total count {}", limitReached ? "" : "NOT", key, count);
		// forward the signal excess event to the output topic 
		context.forward(new Record<String, String>(key, "" + limitReached, endtime));
		if (count > threshold) {	
			removeEventsInWindowByKey(key, startTime, endtime);
		}
    }
    
    private void removeEventsInWindowByKey(String key, long startTime, long endTime) {
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
