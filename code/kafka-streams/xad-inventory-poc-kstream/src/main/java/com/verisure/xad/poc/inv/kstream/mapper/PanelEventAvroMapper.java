package com.verisure.xad.poc.inv.kstream.mapper;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import com.verisure.inv.avro.PanelEventAvroDTO;

public class PanelEventAvroMapper implements KeyValueMapper<String, String, KeyValue<String, PanelEventAvroDTO>> {
	
    @Override
    public KeyValue<String, PanelEventAvroDTO> apply(String key, String value) {
        // Transform the key and value as desired
        String newKey = key;
        PanelEventAvroDTO newValue = PanelEventAvroDTO.newBuilder().build();
        
        // Return a new KeyValue pair
        return new KeyValue<>(newKey, newValue);
    }
}
