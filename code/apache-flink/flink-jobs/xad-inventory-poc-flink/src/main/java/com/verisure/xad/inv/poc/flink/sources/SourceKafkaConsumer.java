package com.verisure.xad.inv.poc.flink.sources;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;
import java.nio.file.StandardCopyOption;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;

public class SourceKafkaConsumer<T> extends FlinkKafkaConsumer<T> {
    private String truststorePath;

    public SourceKafkaConsumer(String topic, DeserializationSchema<T> valueDeserializer, Properties props, String truststore) {
        super(topic, valueDeserializer, props);
        truststorePath = truststore;
    }

    @Override
    public void open(Configuration configuration) throws Exception {

        InputStream truststoreStream = SourceKafkaConsumer.class.getResourceAsStream(truststorePath);
        // Create a temporary file to store the truststore
        File tempFile = File.createTempFile("truststore", ".jks");
        // Copy the truststore from the input stream to the temporary file
        Files.copy(truststoreStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        super.properties.setProperty("ssl.truststore.location", tempFile.getAbsolutePath());

        super.open(configuration);
    }
}