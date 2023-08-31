package com.verisure.xad.poc.inv.kstream.supplier;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.verisure.xad.poc.inv.kstream.processor.SignalExcessLimitExecutionsProcessor;

@Component
@Scope(value = "prototype")
public class SignalExcessLimitExecutionsProcessorSupplier implements ProcessorSupplier<String, Integer, String, String> {

	@Autowired
	private BeanFactory beanFactory;
	
	private String stateStoreName;
	private int threshold;
	private long windowSize;
	
	public SignalExcessLimitExecutionsProcessorSupplier(String stateStoreName, int threshold, long windowSize) {
		this.stateStoreName = stateStoreName;
		this.threshold = threshold;
		this.windowSize = windowSize;
	}
    
	@Override
	public Processor<String, Integer, String, String> get() {
		return beanFactory.getBean(SignalExcessLimitExecutionsProcessor.class, stateStoreName, threshold, windowSize);
	}
}
