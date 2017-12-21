package data.streaming.test;

import data.streaming.utils.AllWindowFunctionImpl;
import data.streaming.utils.LoggingFactory;
import data.streaming.utils.Utils;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;

public class TestFlinkKafkaConsumer {

    public static void main(String... args) throws Exception {

        // set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties props = LoggingFactory.getCloudKarafkaCredentials();

        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        DataStream<String> stream = env.addSource(new FlinkKafkaConsumer010<>(props.getProperty("CLOUDKARAFKA_TOPIC").trim(), new SimpleStringSchema(), props));

        // TODO 4: Hacer algo más interesante que mostrar por pantalla.

       AllWindowFunction<String, String, TimeWindow> apply = new AllWindowFunctionImpl();

        stream.timeWindowAll(Time.seconds(60)).apply(apply)
                .filter(Utils::isTweetValid)
                .map(Utils::createTweetDTO)
                .addSink(Utils::saveTweetInDatabase);

        // execute program
        env.execute("Twitter Streaming Consumer");
    }
}
