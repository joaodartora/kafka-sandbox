package com.joaodartora.kafka.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerDemoWithKeys {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Logger logger = LoggerFactory.getLogger(ProducerDemoWithKeys.class);

        String boostrapServers = "localhost:9092";
        String topic = "test-topic";

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i < 10; i++) {

            String value = "testing kafka producer with keys " + i;
            String key = "id_" + i;

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("Received metadata: Topic {} - Partition {} - Offset {} - Timestamp {}", metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());
                } else {
                    logger.error("Error while producing event", exception);
                }
            }).get();
        }

        producer.flush();
        producer.close();
    }


}
