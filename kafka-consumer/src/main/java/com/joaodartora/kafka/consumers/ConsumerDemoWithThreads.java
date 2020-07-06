package com.joaodartora.kafka.consumers;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThreads {

    public static void main(String[] args) {
        new ConsumerDemoWithThreads().run();
    }

    private ConsumerDemoWithThreads() {
    }

    private void run() {
        Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class);

        String boostrapServers = "localhost:9092";
        String groupId = "fourth-application";
        String topic = "test-topic";
        CountDownLatch latch = new CountDownLatch(1);

        logger.info("Creating the consumer thread!");
        Runnable myConsumerRunnable = new ConsumerRunnable(boostrapServers, groupId, topic, latch);

        Thread myThread = new Thread(myConsumerRunnable);
        myThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook");
            ((ConsumerRunnable) myConsumerRunnable).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Application has exited!");
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted", e);
        } finally {
            logger.info("Application is closing");
        }
    }

    public static class ConsumerRunnable implements Runnable {

        private Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class);
        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;

        public ConsumerRunnable(String boostrapServers,
                                String groupId,
                                String topic,
                                CountDownLatch latch) {
            this.latch = latch;
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            consumer = new KafkaConsumer<String, String>(properties);
            consumer.subscribe(Collections.singletonList(topic));
        }

        public void run() {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key: {} - Value: {}", record.key(), record.value());
                        logger.info("Partition: {} - Offsets: {}", record.partition(), record.offset());
                    }
                }
            } catch (WakeupException exception) {
                logger.info("Received shutdown signal!");
            } finally {
                consumer.close();
                latch.countDown();
            }
        }

        public void shutdown() {
            consumer.wakeup();
        }
    }

}
