/*
 * TO-DO
 */

package com.verisure.xad.inv.poc.generator;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.api.java.utils.ParameterTool;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.errors.SerializationException;

import java.util.Properties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.inv.avro.PanelEvent;
import com.verisure.inv.avro.SdKey;

/**
 * TO-DO
 */
public class VerisureRandomEventKafkaGenerator {

    public static final long WINDOW_SIZE = 1000; //miliseconds
    public static final int EVENTS_PER_WINDOW = 50;
    public static final int NUMBER_INSTALATION_MIN = 1;
    public static final int NUMBER_INSTALATION_MAX = 5000000;
    private static final String TOPIC = "xad.pocinv.panelevents.flink.input";
    private static final String BOOTSTRAP_SERVERS = "broker:29092";
    private static final String SCHEMA_REGISTRY_URL = "http://schema-registry:8091";
    private static final String TRUST_STORE_PATH = "/kafka_broker.truststore.jks";

    @SuppressWarnings("InfiniteLoopStatement")
     public static void main(String[] args) throws Exception {
        // parse input arguments
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);

        /**
        * Parameters:
        * --output-topic <topic>
        * --bootstrap.servers <kafka brokers>
        * --schema-registry-url <confluent schema registry>
        * --ssl-validation
        * --truststore-path <truststore file>
        * --jaas.config.username <jaas.config.username> --jaas.config.password <jaas.config.password>
        * --events-per-window <n events> --window-size <n miliseconds>
        * --id-instalation-min <n instalation> --id-instalation-max <n instalation>
        */

		String outputTopic = parameterTool.get("output-topic", TOPIC);
        int eventsPerWindow = parameterTool.getInt("events-per-window", EVENTS_PER_WINDOW);
        long windowSize = parameterTool.getLong("window-size", WINDOW_SIZE);
        int nInstalationMin = parameterTool.getInt("id-instalation-min", NUMBER_INSTALATION_MIN);
        int nInstalationMax = parameterTool.getInt("id-instalation-max", NUMBER_INSTALATION_MAX);

        Properties props = createKafkaProperties(parameterTool);

        //this calculation is only accurate as long as EVENTS_PER_WINDOW divides the
	    //window size
	    long DELAY = windowSize / eventsPerWindow;

        try (KafkaProducer<String, PanelEventAvroDTO> producer = new KafkaProducer<String, PanelEventAvroDTO>(props)) {

            while (true) {
                final int instalation = getRandomNumInstalation(nInstalationMin, nInstalationMax);
                final String key = "ESP_" + Long.toString(instalation);
                final SdKey sdKey = new SdKey("ESP", instalation, Long.valueOf(164263),  Long.valueOf(164263));
                final PanelEvent panelEvent = new PanelEvent("N", "GPRS", 101, "VECU", "QR", 0, "MAN", "panel", false, "RPI", "INPUT", "IPLABEL", sdKey, "22011922594700002DAAVHR3", "MAN", 2, "RPI", "ORAN", 4336609, false, "PANEL", "MACHINE",  Long.valueOf(164263), 3, "EVENT", "UTC+01:00", "QR01");
                final PanelEventAvroDTO panelEventAvroDTO = new PanelEventAvroDTO(panelEvent);
                final ProducerRecord<String, PanelEventAvroDTO> record = new ProducerRecord<String, PanelEventAvroDTO>(outputTopic, key, panelEventAvroDTO);
                producer.send(record);
                Thread.sleep(DELAY);
            }

        } catch (final SerializationException e) {
            e.printStackTrace();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
         
    }

    private static int getRandomNumInstalation(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private static Properties createKafkaProperties(final ParameterTool parameterTool) {

		String bootstrapServers = parameterTool.get("bootstrap.servers", BOOTSTRAP_SERVERS);
		String schemaRegistryUrl = parameterTool.get("schema-registry-url", SCHEMA_REGISTRY_URL);

        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        boolean sslValidation = parameterTool.has("ssl-validation");

        if (sslValidation) {
            // Specify the path to your truststore file inside the jar file
            String truststorePath = parameterTool.get("truststore-path", TRUST_STORE_PATH);
            try {
                // Create an input stream to read the truststore file
                InputStream truststoreStream = VerisureRandomEventKafkaGenerator.class.getResourceAsStream(truststorePath);
                // Create a temporary file to store the truststore
                File tempFile = File.createTempFile("truststore", ".jks");
                // Copy the truststore from the input stream to the temporary file
                Files.copy(truststoreStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // SSL configurations
                // Configure the path of truststore (CA) provided by the server
                props.put("ssl.truststore.location", tempFile.getAbsolutePath());
                props.put("ssl.truststore.password", "confluenttruststorepass");
                //System.setProperty("javax.net.ssl.trustStore", tempFile.getAbsolutePath());
                //System.setProperty("javax.net.ssl.trustStorePassword", "confluenttruststorepass");
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            
            // SASL configurations
            props.put("security.protocol", "SASL_SSL");
            // Set SASL mechanism as SCRAM-SHA-512
            props.put("sasl.mechanism", "SCRAM-SHA-512");
            // Set JAAS configurations
            String jaasUsername = parameterTool.get("jaas.config.username");
            String jaasPassword = parameterTool.get("jaas.config.password");
            props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\""+jaasUsername+"\" password=\""+jaasPassword+"\";");
        }

		return props;
	}
}

