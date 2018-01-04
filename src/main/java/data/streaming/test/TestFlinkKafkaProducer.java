
package data.streaming.test;

import data.streaming.db.MongoConnector;
import data.streaming.utils.LoggingFactory;
import data.streaming.waux.ValidTagsTweetEndpointInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;
import java.util.SortedSet;

public class TestFlinkKafkaProducer {

    private static final Integer PARALLELISM = 2;
    private static MongoConnector mongoConnector = new MongoConnector();

    public static void main(String... args) throws Exception {

        TwitterSource twitterSource = new TwitterSource(LoggingFactory.getTwitterCredentias());

        SortedSet<String> keywordsHashTags = mongoConnector.getKeywordsHashTags();
        final String[] KEYWORDS = keywordsHashTags.toArray(new String[keywordsHashTags.size()]);

        // Set filter
        twitterSource.setCustomEndpointInitializer(new ValidTagsTweetEndpointInitializer(KEYWORDS));

        // set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(PARALLELISM);

        // Add source and generate the stream as output of calls async to save data in MongoDB
        DataStream<String> stream = env.addSource(twitterSource);

        Properties props = LoggingFactory.getCloudKarafkaCredentials();

        FlinkKafkaProducer010.FlinkKafkaProducer010Configuration<String> config = FlinkKafkaProducer010
                .writeToKafkaWithTimestamps(stream, props.getProperty("CLOUDKARAFKA_TOPIC").trim(), new SimpleStringSchema(), props);
        config.setWriteTimestampToKafka(false);
        config.setLogFailuresOnly(false);
        config.setFlushOnCheckpoint(true);

        stream.print();

        env.execute("Twitter Streaming Producer");
    }
}
