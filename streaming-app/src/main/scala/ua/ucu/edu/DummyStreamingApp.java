package ua.ucu.edu;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DummyStreamingApp {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(DummyStreamingApp.class);

        Properties config = new Properties();
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "streaming_app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("KAFKA_BROKERS"));
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> twitter_topic = builder.stream("twitter-topic");

        KTable<String, String> reddit_table = builder.table("reddit-topic");

        KStream<String, String> TwitterReddit =
                twitter_topic.leftJoin(reddit_table,
                        (twitter_top, reddit_top) -> {
                            if (reddit_top != null) {
                                List<String> words1 = Arrays.asList(reddit_top.split(" "));
                                List<String> words2 = Arrays.asList(twitter_top.split(" "));
                                if (words2.containsAll(words1)) {
                                    logger.info("Reddit=" + reddit_top + ",Twitter=[" + twitter_top + "]");
                                    return "Reddit=" + reddit_top + ",Twitter=[" + twitter_top + "]";
                                } else {
                                    logger.info("Reddit=" + reddit_top + ",Twitter=null");
                                    return "Reddit=" + reddit_top + ",Twitter=null";
                                }
                            } else {
                                logger.info("Twitter=" + twitter_top + ",Reddit=null");
                                return "Twitter=" + twitter_top + ",Reddit=null";
                            }
                        }
                );
        TwitterReddit.to("test-topic-out");

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.cleanUp();
        streams.start();

        // print the topology
        System.out.println(streams.toString());

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

}