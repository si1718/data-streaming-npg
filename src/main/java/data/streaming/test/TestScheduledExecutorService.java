package data.streaming.test;

import data.streaming.scheduled.Batcher;

public class TestScheduledExecutorService {

    public static void main(String... args) throws Exception {

        Batcher batcher = new Batcher();

        batcher.prepare();
    }
}
