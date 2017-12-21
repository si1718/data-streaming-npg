package data.streaming.scheduled;

import data.streaming.db.MongoConnector;
import data.streaming.dto.Chapter;
import data.streaming.dto.CustomTriple;
import data.streaming.dto.CustomTuple;
import data.streaming.dto.Tweet;
import data.streaming.utils.Utils;
import org.apache.commons.collections.map.HashedMap;
import org.bson.Document;

import javax.print.Doc;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static data.streaming.db.MongoConnector.RATINGS_COLLECTION;
import static data.streaming.db.MongoConnector.REPORTS_COLLECTION;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Batcher {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Integer INITIAL_DELAY = 0; // In seconds
    private final Integer PERIOD = 60 * 60 * 24; // In seconds => 1 day
    private final Integer SECONDS_TO_CANCEL = 60 * 60; // In seconds

    private final Integer MIN_IN_RATING = 0;
    private final Integer MIN_OUT_RATING = 1;
    private final Integer MAX_OUT_RATING = 5;

    private final String DAILY_REPORT_TYPE = "daily_report";
    private final String MONTHLY_REPORT_TYPE = "monthly_report";

    private final MongoConnector mongoConnector = new MongoConnector();

    public void prepare() {

        final Runnable scheduled = this::start;

        final ScheduledFuture<?> scheduledHandle = scheduler.scheduleAtFixedRate(scheduled, INITIAL_DELAY, PERIOD, SECONDS);

//        scheduler.schedule((Runnable) () -> scheduledHandle.cancel(true), SECONDS_TO_CANCEL, SECONDS);
    }

    private SortedSet<String> getKeywords() {
        return mongoConnector.getKeywords();
    }

    private List<Chapter> getChapters() {
        return mongoConnector.getChaptersWithKeywords();
    }

    private void start() {

        System.out.printf("%n Initializing batch... %n%n");

        saveStatistics();

        saveRatings();

        System.out.printf("%n Batch finished. %n%n");
    }

    private void saveRatings() {

        System.out.printf("%n Executing statistics task... %n%n");

        System.out.printf("%n Cleaning previous data in collection %s ... %n%n", RATINGS_COLLECTION);
        mongoConnector.cleanCollection(RATINGS_COLLECTION);
        System.out.printf("%n Data cleaned. %n");

        List<Chapter> chapters = getChapters();
        List<CustomTriple<String, String, Integer>> tupleList = new ArrayList<>();

        Integer max = 0;

        for (Chapter chapterA : chapters) {

            max = Integer.MIN_VALUE;

            for (Chapter chapterB : chapters) {
                if (!chapterA.equals(chapterB)) {

                    SortedSet<String> intersection = new TreeSet<>(chapterA.getKeywords()); // use the copy constructor
                    intersection.retainAll(chapterB.getKeywords());

                    CustomTriple<String, String, Integer> tuple = new CustomTriple<>(chapterA.getIdChapter(), chapterB.getIdChapter(), intersection.size());
                    tupleList.add(tuple);

                    max = Math.max(max, intersection.size());
                }
            }

            // If max is equals to min_in_rating meanings that any chapter has any keyword in common with other chapters
            if (max > MIN_IN_RATING) {

                for (CustomTriple<String, String, Integer> tuple : tupleList) {

                    System.out.printf("%n Saving tuple <%s, %s, %d> ... %n%n", tuple.getLeft(), tuple.getMiddle(), tuple.getRight());

                    tuple.setRight(Utils.mapValue(tuple.getRight(), MIN_IN_RATING, max, MIN_OUT_RATING, MAX_OUT_RATING));

                    Document document = new Document();
                    document.append("chapter_a", tuple.getLeft());
                    document.append("chapter_b", tuple.getMiddle());
                    document.append("rating", tuple.getRight());

                    mongoConnector.populateCollection(RATINGS_COLLECTION, document);

                    System.out.printf("%n Data saved. %n%n");
                }
            }

            tupleList.clear();
        }

        System.out.printf("%n Statistics task finished. %n%n");
    }

    private void saveStatistics() {

        System.out.printf("%n Executing statistics task... %n%n");

        SortedSet<String> keywords = getKeywords();

        System.out.printf("%n Keywords found %s %n%n", keywords);

        Map<String, List<CustomTuple<Date, Integer>>> dailyReport = new HashMap<>();
        Map<String, List<CustomTuple<Date, Integer>>> monthlyReport = new HashMap<>();

        keywords.forEach(keyword -> {

            List<Tweet> tweets = mongoConnector.getTweetsByKeyword(keyword);

            List<CustomTuple<Date, Integer>> daily = getDateCountByType(tweets, DAILY_REPORT_TYPE);
            dailyReport.put(keyword, daily);

            List<CustomTuple<Date, Integer>> monthly = getDateCountByType(tweets, MONTHLY_REPORT_TYPE);
            monthlyReport.put(keyword, monthly);
        });

        System.out.printf("%n Cleaning previous data in collection %s ... %n%n", REPORTS_COLLECTION);
        mongoConnector.cleanCollection(REPORTS_COLLECTION);
        System.out.printf("%n Data cleaned. %n");

        dailyReport.forEach((keyword, tupleLists) -> {

            tupleLists.forEach(tuple -> {

                saveDocument(keyword, tuple, DAILY_REPORT_TYPE);
            });
        });

        monthlyReport.forEach((keyword, tupleLists) -> {

            tupleLists.forEach(tuple -> {

                saveDocument(keyword, tuple, MONTHLY_REPORT_TYPE);
            });
        });

        System.out.printf("%n Statistics task finished. %n%n");
    }

    private void saveDocument(String keyword, CustomTuple<Date, Integer> tuple, String reportType) {

        System.out.printf("%n Saving data from the keyword %s as of %s ... %n%n", keyword, tuple.getLeft().toString());

        Document document = new Document();
        document.append("keyword", keyword);
        document.append("date", Utils.convertDateToISO8601(tuple.getLeft()));
        document.append("count", tuple.getRight());
        document.append("type", reportType);

        mongoConnector.populateCollection(REPORTS_COLLECTION, document);

        System.out.printf("%n Data saved. %n%n");
    }

    private List<CustomTuple<Date, Integer>> getDateCountByType(List<Tweet> tweets, String reportType) {

        List<CustomTuple<Date, Integer>> information = new ArrayList<>();

        tweets.forEach(tweet -> {

            List<CustomTuple<Date, Integer>> result = information.stream()
                    .filter(item -> isSameItem(item, tweet, reportType))
                    .collect(Collectors.toList());

            if (result.isEmpty()) {

                information.add(new CustomTuple<>(tweet.getDate(), 1));

            } else {

                result.forEach(tuple -> {
                    tuple.setRight(tuple.getRight() + 1);
                });
            }

        });

        return information;
    }

    private boolean isSameItem(CustomTuple<Date, Integer> item, Tweet tweet, String reportType) {

        boolean result;

        switch (reportType) {
            case DAILY_REPORT_TYPE:
                result = isSameDayMonthAndYear(item, tweet);
                break;
            case MONTHLY_REPORT_TYPE:
                result = isSameMonthOfYear(item, tweet);
                break;
            default:
                result = isSameDayMonthAndYear(item, tweet);
                break;
        }

        return result;
    }

    private boolean isSameMonthOfYear(CustomTuple<Date, Integer> item, Tweet tweet) {

        Calendar calendarA = Calendar.getInstance();
        Calendar calendarB = Calendar.getInstance();

        calendarA.setTime(tweet.getDate());
        calendarB.setTime(item.getLeft());

        return (calendarA.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR)) && (calendarA.get(Calendar.MONTH) == calendarB.get(Calendar.MONTH));
    }

    private boolean isSameDayMonthAndYear(CustomTuple<Date, Integer> item, Tweet tweet) {

        Calendar calendarA = Calendar.getInstance();
        Calendar calendarB = Calendar.getInstance();

        calendarA.setTime(tweet.getDate());
        calendarB.setTime(item.getLeft());

        return (calendarA.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR))
                && (calendarA.get(Calendar.MONTH) == calendarB.get(Calendar.MONTH))
                && (calendarA.get(Calendar.DATE) == calendarB.get(Calendar.DATE));
    }
}
