package com.verisure.xad.poc.inv.kstream.supplier;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.xad.poc.inv.kstream.processor.SignalExcessProcessor;

@Component
@Scope(value = "prototype")
public class SignalExcessProcessorSupplier implements ProcessorSupplier<String, PanelEventAvroDTO, String, String> {

	@Autowired
	private BeanFactory beanFactory;
	
	private String stateStoreName;
	private int threshold;
	private long windowSize;
	
	public SignalExcessProcessorSupplier(String stateStoreName, int threshold, long windowSize) {
		this.stateStoreName = stateStoreName;
		this.threshold = threshold;
		this.windowSize = windowSize;
	}
    
	@Override
	public Processor<String, PanelEventAvroDTO, String, String> get() {
		return beanFactory.getBean(SignalExcessProcessor.class, stateStoreName, threshold, windowSize);
	}
}
