package com.verisure.xad.poc.inv.kstream.supplier;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.verisure.inv.avro.MissingTestEventAvro;
import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.xad.poc.inv.kstream.processor.MissingTestProcessor;

@Component
@Scope(value = "prototype")
public class MissingTestProcessorSupplier implements ProcessorSupplier<String, PanelEventAvroDTO, String, MissingTestEventAvro> {

	@Autowired
	private BeanFactory beanFactory;
	
	private String stateStoreName;
	private long windowSize;
	
	public MissingTestProcessorSupplier(String stateStoreName, long windowSize) {
		this.windowSize = windowSize;
		this.stateStoreName = stateStoreName;
	}
    
	@Override
	public Processor<String, PanelEventAvroDTO, String, MissingTestEventAvro> get() {
		return beanFactory.getBean(MissingTestProcessor.class, stateStoreName, windowSize);
	}
	
}
