package data.streaming.utils;

import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class AllWindowFunctionImpl implements AllWindowFunction<String, String, TimeWindow> {

    @Override
    public void apply(TimeWindow timeWindow, Iterable<String> iterable, Collector<String> collector) throws Exception {

        for(String s: iterable) {
            collector.collect(s);
        }
    }
}
