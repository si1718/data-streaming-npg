
package data.streaming.test;

import data.streaming.db.MongoConnection;
import data.streaming.utils.LoggingFactory;
import data.streaming.waux.ValidTagsTweetEndpoIntinitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;
import java.util.SortedSet;

public class TestFlinkKafkaProducer {

    private static final Integer PARALLELISM = 2;

    public static void main(String... args) throws Exception {

        TwitterSource twitterSource = new TwitterSource(LoggingFactory.getTwitterCredentias());

        MongoConnection mongoConnection = new MongoConnection();
        SortedSet<String> keywords = mongoConnection.getKeywords();

        final String[] KEYWORDS = keywords.toArray(new String[keywords.size()]);

        // Establecemos el filtro
        twitterSource.setCustomEndpointInitializer(new ValidTagsTweetEndpoIntinitializer(KEYWORDS));

        // set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(PARALLELISM);

        // Añadimos la fuente y generamos el stream como la salida de las llamadas
        // asíncronas para salvar los datos en MongoDB
        DataStream<String> stream = env.addSource(twitterSource);

        Properties props = LoggingFactory.getCloudKarafkaCredentials();

        FlinkKafkaProducer010.FlinkKafkaProducer010Configuration<String> config = FlinkKafkaProducer010
                .writeToKafkaWithTimestamps(stream, props.getProperty("CLOUDKARAFKA_TOPIC").trim(), new SimpleStringSchema(),
                        props);
        config.setWriteTimestampToKafka(false);
        config.setLogFailuresOnly(false);
        config.setFlushOnCheckpoint(true);

        stream.print();

        env.execute("Twitter Streaming Producer");
    }
}
