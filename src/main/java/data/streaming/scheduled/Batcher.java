package data.streaming.scheduled;

import data.streaming.db.MongoConnection;
import data.streaming.dto.Chapter;
import data.streaming.dto.CustomTuple;
import data.streaming.dto.TweetDTO;
import data.streaming.utils.Utils;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static data.streaming.db.MongoConnection.CHAPTERS_RATING_COLLECTION;
import static data.streaming.db.MongoConnection.KEYWORD_DATE_TWEETS_COLLECTION;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Batcher {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Integer INITIAL_DELAY = 0; // In seconds
    private final Integer PERIOD = 60 * 60 * 24; // In seconds => 1 day
//    private final Integer PERIOD = 60; // In seconds => 1 day
    private final Integer SECONDS_TO_CANCEL = 60 * 60; // In seconds

    private final Integer MIN_IN_RATING = 0;
    private final Integer MIN_OUT_RATING = 1;
    private final Integer MAX_OUT_RATING = 5;

    private final MongoConnection mongoConnection = new MongoConnection();

    public void prepare() {

        final Runnable scheduled = this::start;

        final ScheduledFuture<?> scheduledHandle = scheduler.scheduleAtFixedRate(scheduled, INITIAL_DELAY, PERIOD, SECONDS);

//        scheduler.schedule((Runnable) () -> scheduledHandle.cancel(true), SECONDS_TO_CANCEL, SECONDS);
    }

    private SortedSet<String> getKeywords() {
        return mongoConnection.getKeywords();
    }

    private List<Chapter> getChapters() {
        return mongoConnection.getChapters();
    }

    private void start() {

        System.out.printf("%n Initializing batch... %n%n");

        saveKeywordDateTweetsStatistics();

        saveChapterChapterRatingStatistics();

        System.out.printf("%n Batch finished. %n%n");
    }

    private void saveChapterChapterRatingStatistics() {

        System.out.printf("%n Executing <chapter_a, chapter_b, rating> statistics task... %n%n");

        List<Chapter> chapters = getChapters();
        List<CustomTuple<String, String, Integer>> tupleList = new ArrayList<>();

        Integer max = Integer.MIN_VALUE;

        for (Chapter chapterA : chapters) {
            for (Chapter chapterB : chapters) {
                if (!chapterA.equals(chapterB)) {

                    SortedSet<String> intersection = new TreeSet<>(chapterA.getKeywords()); // use the copy constructor
                    intersection.retainAll(chapterB.getKeywords());

                    CustomTuple<String, String, Integer> tuple = new CustomTuple<>(chapterA.getIdChapter(), chapterB.getIdChapter(), intersection.size());
                    tupleList.add(tuple);

                    max = Math.max(max, intersection.size());
                }
            }
        }

        System.out.printf("%n Cleaning previous data in collection %s ... %n%n", CHAPTERS_RATING_COLLECTION);
        mongoConnection.cleanCollection(CHAPTERS_RATING_COLLECTION);
        System.out.printf("%n Data cleaned. %n");

        // If max is equals to min_in_rating meanings that any chapter has any keyword in common with other chapters
        if(max > MIN_IN_RATING) {

            for(CustomTuple<String, String, Integer> tuple : tupleList) {

                System.out.printf("%n Saving tuple <%s, %s, %d> ... %n%n", tuple.getLeft(), tuple.getMiddle(), tuple.getRight());

                tuple.setRight(Utils.mapValue(tuple.getRight(), MIN_IN_RATING, max, MIN_OUT_RATING, MAX_OUT_RATING));

                Document document = new Document();
                document.append("chapter_a", tuple.getLeft());
                document.append("chapter_b", tuple.getMiddle());
                document.append("rating", tuple.getRight());

                mongoConnection.populateCollection(CHAPTERS_RATING_COLLECTION, document);

                System.out.printf("%n Data saved. %n%n");
            }
        }

        System.out.printf("%n <chapter_a, chapter_b, rating> statistics task finished. %n%n");
    }

    private void saveKeywordDateTweetsStatistics() {

        System.out.printf("%n Executing <keyword, date, tweets> statistics task... %n%n");

        SortedSet<String> keywords = getKeywords();

        System.out.printf("%n Keywords found %s %n%n", keywords);

        Map<String, Map<String, List<TweetDTO>>> map = new HashMap<>();

        keywords.forEach(keyword -> {
            Map<String, List<TweetDTO>> tweets = mongoConnection.getTweetsByKeywordAndDate(keyword);
            map.put(keyword, tweets);
        });

        System.out.printf("%n Cleaning previous data in collection %s ... %n%n", KEYWORD_DATE_TWEETS_COLLECTION);
        mongoConnection.cleanCollection(KEYWORD_DATE_TWEETS_COLLECTION);
        System.out.printf("%n Data cleaned. %n");

        map.forEach((keyword, values) -> {
            values.forEach((date, tweets) -> {

                System.out.printf("%n Saving data from the keyword %s as of %s ... %n%n", keyword, date);

                Document document = new Document();
                document.append("keyword", keyword);
                document.append("date", date);
                document.append("count", tweets.size());

                mongoConnection.populateCollection(KEYWORD_DATE_TWEETS_COLLECTION, document);

                System.out.printf("%n Data saved. %n%n");
            });
        });

        System.out.printf("%n <keyword, date, tweets> statistics task finished. %n%n");
    }
}
