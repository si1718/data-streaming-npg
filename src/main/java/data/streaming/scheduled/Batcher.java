package data.streaming.scheduled;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Batcher {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Integer INITIAL_DELAY = 0; // In seconds
    private final Integer PERIOD = 60 * 60 * 24; // In seconds => 1 day
    private final Integer SECONDS_TO_CANCEL = 60 * 60; // In seconds

    private final Ratings ratings = new Ratings();
    private final Reports reports = new Reports();
    private final Recommender recommender = new Recommender();

    public void prepare() {

        final Runnable scheduled = this::start;

        final ScheduledFuture<?> scheduledHandle = scheduler.scheduleAtFixedRate(scheduled, INITIAL_DELAY, PERIOD, SECONDS);

//        scheduler.schedule((Runnable) () -> scheduledHandle.cancel(true), SECONDS_TO_CANCEL, SECONDS);
    }

    private void start() {

        System.out.printf("%n Initializing batch... %n%n");

        ratings.run();
        recommender.run();
        reports.run();

        System.out.printf("%n Batch finished. %n%n");
    }
}
