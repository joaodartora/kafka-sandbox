package com.joaodartora.kafka.streams;

import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class StreamsFilterTweets {

    private static final JsonParser jsonParser = new JsonParser();

    private static final String bootstrapServers = "localhost:9092";
    private static final String groupId = "demo-kafka-streams";
    private static final String inputTopicName = "twitter-tweets";
    private static final String outputTopicName = "important-tweets";
    private static final Integer NUMBER_OF_TWEETS_TO_BE_IMPORTANT = 10000;

    public static void main(String[] args) {
        Properties properties = buildKafkaProperties();

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> inputTopicStream = streamsBuilder.stream(inputTopicName);

        KStream<String, String> filteredStream = inputTopicStream
                .filter((key, jsonTweet) -> extractUserFollowersInTweet(jsonTweet) > NUMBER_OF_TWEETS_TO_BE_IMPORTANT);

        filteredStream.to(outputTopicName);

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);

        kafkaStreams.start();
    }

    private static Properties buildKafkaProperties() {
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, groupId);
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        return properties;
    }

    private static Integer extractUserFollowersInTweet(String tweetJson) {
        try {
            return jsonParser.parse(tweetJson)
                    .getAsJsonObject()
                    .get("user")
                    .getAsJsonObject()
                    .get("followers_count")
                    .getAsInt();
        } catch (NullPointerException exception) {
            return 0;
        }
    }

}
