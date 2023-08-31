package com.verisure.xad.inv.poc.flink.sinks;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;
import java.nio.file.StandardCopyOption;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;

public class SinkKafkaProducer<IN> extends FlinkKafkaProducer<IN>{
    private String truststorePath;

    public SinkKafkaProducer(String topicId, SerializationSchema<IN> serializationSchema, Properties producerConfig, String truststore) {
        super(topicId, serializationSchema, producerConfig);
        truststorePath = truststore;
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {

        InputStream truststoreStream = SinkKafkaProducer.class.getResourceAsStream(truststorePath);
        // Create a temporary file to store the truststore
        File tempFile = File.createTempFile("truststore", ".jks");
        // Copy the truststore from the input stream to the temporary file
        Files.copy(truststoreStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        producerConfig.setProperty("ssl.truststore.location", tempFile.getAbsolutePath());

        super.initializeState(context);
    }
}