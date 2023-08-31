/*
 * TO-DO
 */

package com.verisure.xad.inv.poc.flink;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.inv.avro.PanelEvent;
import com.verisure.inv.avro.SdKey;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.nio.file.StandardCopyOption;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

import com.verisure.xad.inv.poc.flink.sinks.SinkKafkaProducer;
import com.verisure.xad.inv.poc.flink.sources.SourceKafkaConsumer;
import com.verisure.xad.inv.poc.flink.functions.MyProcessWindowFunction;
import com.verisure.xad.inv.poc.flink.functions.MyProcessWindowFunction2;
import com.verisure.xad.inv.poc.flink.functions.MyCustomTriggerFunction;
import com.verisure.xad.inv.poc.flink.functions.MyKeyedProcessFunction;

/**
 * TO-DO
 */
public class SignalExcessJob {

    private static final String INPUT_TOPIC = "xad.pocinv.panelevents.flink.input";
	private static final String OUTPUT_TOPIC = "xad.pocinv.panelevents.flink.output";
	//private static final String BOOTSTRAP_SERVERS = "ef1brkm2m01v.vsdepi.local:9093,ef1brkm2m02v.vsdepi.local:9093,ef1brkm2m03v.vsdepi.local:9093";
    private static final String BOOTSTRAP_SERVERS = "broker:29092";
	//private static final String SCHEMA_REGISTRY_URL = "https://ef1regm2m01v.vsdepi.local:8081";
    private static final String SCHEMA_REGISTRY_URL = "http://schema-registry:8091";
	private static final String GROUP_ID = "xad.pocinv.panelevents.flink.consumer";
	private static final int THRESHOLD = 20;
    private static final String TRUST_STORE_PATH = "/kafka_broker.truststore.jks";

	private static final Logger LOG = LoggerFactory.getLogger(SignalExcessJob.class);

	public static void main(String[] args) throws Exception {
        try {

            // parse input arguments
            final ParameterTool parameterTool = ParameterTool.fromArgs(args);

            /**
            * Parameters:
            * --input-topic <topic> --output-topic <topic>
            * --bootstrap.servers <kafka brokers>
            * --schema-registry-url <confluent schema registry> --group.id <some id>
            * --ssl-validation
            * --truststore-path <truststore file>
            * --jaas.config.username <jaas.config.username> --jaas.config.password <jaas.config.password>
            * --threshold <n threshold>
            */

            String inputTopic = parameterTool.get("input-topic", INPUT_TOPIC);
            String outputTopic = parameterTool.get("output-topic", OUTPUT_TOPIC);
            String schemaRegistryUrl = parameterTool.get("schema-registry-url", SCHEMA_REGISTRY_URL);
            int threshold = parameterTool.getInt("threshold", THRESHOLD);
            String truststorePath = parameterTool.get("truststore-path", TRUST_STORE_PATH);
            boolean sslValidation = parameterTool.has("ssl-validation");

            Properties config = createKafkaProperties(parameterTool);


            // Sets up the execution environment, which is the main entry point
            // to building Flink applications.
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            // start a checkpoint every 30000 ms
		    env.enableCheckpointing(30000);

            // Create Source inputs
		    FlinkKafkaConsumer<PanelEventAvroDTO> localConsumer = new FlinkKafkaConsumer<>(
															inputTopic, 
															ConfluentRegistryAvroDeserializationSchema.forSpecific(PanelEventAvroDTO.class, schemaRegistryUrl),
															config);
            
            DataStream<PanelEventAvroDTO> input;

            input = env.addSource(localConsumer, "Signal Source");

            DataStream< Tuple2<String, String> > dataStructure = input.flatMap(new FlatMapFunction<PanelEventAvroDTO, Tuple2<String, String> >()
												{
													public void flatMap(PanelEventAvroDTO value, Collector< Tuple2<String, String> > out)
													{
														PanelEvent panelEvent = value.getPanelEvent();
														SdKey sdKey = panelEvent.getSdKey();
														String output = sdKey.getIdCountry() + "_" + sdKey.getIdInstallation();
														out.collect(new Tuple2<String, String>(output, output));
													}	
												});
            
            DataStream< Tuple2<String, String> > firstWindow = dataStructure
                .keyBy(t -> t.f0)
                .window(TumblingProcessingTimeWindows.of(Time.hours(1)))
                //.window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                .trigger(new MyCustomTriggerFunction(threshold))
                .process(new MyProcessWindowFunction(threshold))
                //.trigger(new MyCustomTriggerFunction(threshold))
                //.process(new MyProcessWindowFunction())
                .name("First Event Counter");
            
            //firstWindow.print();

            DataStream< String > secondWindow = firstWindow
                .keyBy(t -> t.f0)
                //.window(TumblingProcessingTimeWindows.of(Time.days(7)))
                //.window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                //.trigger(new MyCustomTriggerFunction(2))
                //.process(new MyProcessWindowFunction2())
                .process(new MyKeyedProcessFunction())
                .name("Second Event Counter");

            SinkKafkaProducer<String> verisureSink = new SinkKafkaProducer<>(outputTopic, new SimpleStringSchema(), config, truststorePath);

            FlinkKafkaProducer<String> localSink = new FlinkKafkaProducer<>(outputTopic, new SimpleStringSchema(), config);

            if (sslValidation) {
                secondWindow.addSink(verisureSink).name("kafka_sink").setParallelism(1).disableChaining();
            } else {
                secondWindow.addSink(localSink).name("kafka_sink").setParallelism(1).disableChaining();
            }

            // Execute program, beginning computation.
            env.execute("Flink Signal Excess Job");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
	}

    private static Properties createKafkaProperties(final ParameterTool parameterTool) {

		String bootstrapServers = parameterTool.get("bootstrap.servers", BOOTSTRAP_SERVERS);
		String schemaRegistryUrl = parameterTool.get("schema-registry-url", SCHEMA_REGISTRY_URL);
        String groupId = parameterTool.get("group.id", GROUP_ID);

        Properties config = new Properties();

        config.setProperty("bootstrap.servers", bootstrapServers);
		config.setProperty("group.id", groupId);
        config.setProperty("auto.commit.interval.ms", "100");

        boolean sslValidation = parameterTool.has("ssl-validation");

        if (sslValidation) {
            // Specify the path to your truststore file inside the jar file
            String truststorePath = parameterTool.get("truststore-path", TRUST_STORE_PATH);
            try {
                // Create an input stream to read the truststore file
                InputStream truststoreStream = SignalExcessJob.class.getResourceAsStream(truststorePath);
                // Create a temporary file to store the truststore
                File tempFile = File.createTempFile("truststore", ".jks");
                // Copy the truststore from the input stream to the temporary file
                Files.copy(truststoreStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // SSL configurations
                // Configure the path of truststore (CA) provided by the server
                config.put("ssl.truststore.location", tempFile.getAbsolutePath());
                config.put("ssl.truststore.password", "confluenttruststorepass");
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            
            // SASL configurations
            config.put("security.protocol", "SASL_SSL");
            // Set SASL mechanism as SCRAM-SHA-512
            config.put("sasl.mechanism", "SCRAM-SHA-512");
            // Set JAAS configurations
            String jaasUsername = parameterTool.get("jaas.config.username");
            String jaasPassword = parameterTool.get("jaas.config.password");
            config.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\""+jaasUsername+"\" password=\""+jaasPassword+"\";");
        }

		return config;
	}
}
