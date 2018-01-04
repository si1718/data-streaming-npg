package data.streaming.scheduled;

import data.streaming.db.MongoConnector;
import data.streaming.dto.*;
import data.streaming.utils.Utils;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static data.streaming.db.MongoConnector.*;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Batcher {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Integer INITIAL_DELAY = 0; // In seconds
    private final Integer PERIOD = 60 * 60 * 24; // In seconds => 1 day
    private final Integer SECONDS_TO_CANCEL = 60 * 60; // In seconds

    private final Integer MIN_IN_RATING = 0;
    private final Integer MIN_OUT_RATING = 0;
    private final Integer MAX_OUT_RATING = 5;

    private final String DAILY_REPORT_TYPE = "daily_report";
    private final String MONTHLY_REPORT_TYPE = "monthly_report";

    private final MongoConnector mongoConnector = new MongoConnector();

    public void prepare() {

        final Runnable scheduled = this::start;

        final ScheduledFuture<?> scheduledHandle = scheduler.scheduleAtFixedRate(scheduled, INITIAL_DELAY, PERIOD, SECONDS);

//        scheduler.schedule((Runnable) () -> scheduledHandle.cancel(true), SECONDS_TO_CANCEL, SECONDS);
    }

    private void start() {

        System.out.printf("%n Initializing batch... %n%n");

        saveReports();
        saveRatings();

        System.out.printf("%n Batch finished. %n%n");
    }

    private void saveRatings() {

        List<Document> documents = new ArrayList<>();
        List<Rating> ratings = new ArrayList<>();
        List<CustomTriple<String, String, Integer>> tupleList = new ArrayList<>();
        List<Chapter> chapters;
        Integer max;

        System.out.printf("%n Executing ratings task... %n%n");

        chapters = mongoConnector.getChaptersWithKeywords();

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

                    Double score = Utils.mapValue(tuple.getRight(), MIN_IN_RATING, max, MIN_OUT_RATING, MAX_OUT_RATING).doubleValue();

                    if(score > 0.0) {
                        Rating rating = new Rating(tuple.getLeft(), tuple.getMiddle(), score);
                        ratings.add(rating);
                    }
                }
            }

            tupleList.clear();
        }

        ratings.forEach(x -> {
            Document document = new Document();
            document.append("chapter_a", x.getChapterA());
            document.append("chapter_b", x.getChapterB());
            document.append("rating", x.getRating());
            documents.add(document);
        });

        mongoConnector.cleanCollection(RATINGS_COLLECTION);

        System.out.printf("%n Saving ratings... %n%n");
        mongoConnector.populateCollection(RATINGS_COLLECTION, documents);
        System.out.printf("%n Ratings saved. %n%n");

        System.out.printf("%n Ratings task finished. %n%n");
    }

    private void saveReports() {

        Map<String, List<CustomTuple<Date, Integer>>> dailyReport = new HashMap<>();
        Map<String, List<CustomTuple<Date, Integer>>> monthlyReport = new HashMap<>();
        List<Document> documents = new ArrayList<>();
        SortedSet<Report> newReports = new TreeSet<>();

        System.out.printf("%n Executing statistics task... %n%n");

        SortedSet<Report> reports = mongoConnector.getReports();
        SortedSet<String> keywords = mongoConnector.getKeywords();

        System.out.printf("%n Keywords found %s %n%n", keywords);

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

        System.out.printf("%n Cleaning previous data in collection %s ... %n%n", TWEETS_COLLECTION);
        mongoConnector.cleanCollection(TWEETS_DATABASE, TWEETS_COLLECTION);
        System.out.printf("%n Data cleaned. %n");

        loadNewReports(dailyReport, DAILY_REPORT_TYPE, newReports);
        loadNewReports(monthlyReport, MONTHLY_REPORT_TYPE, newReports);

        newReports.forEach(a -> {

            Report found = findSameDateReport(reports, a);

            if(found == null) {
                reports.add(a);
            } else {
                found.setCount(found.getCount() + a.getCount());
            }
        });

        reports.forEach(report -> {
            documents.add(report.toDocument());
        });

        mongoConnector.populateCollection(REPORTS_COLLECTION, documents);

        System.out.printf("%n Statistics task finished. %n%n");
    }

    private void loadNewReports(Map<String, List<CustomTuple<Date, Integer>>> reportsRaw, String type, SortedSet<Report> reports) {

        reportsRaw.forEach((keyword, tupleLists) -> {

            tupleLists.forEach(tuple -> {

                String date = Utils.convertDateToISO8601(tuple.getLeft());
                Integer count = tuple.getRight();

                Report report = new Report(keyword, date, count, type);
                reports.add(report);
            });
        });
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
            case MONTHLY_REPORT_TYPE:
                result = isSameMonthOfYear(item.getLeft(), tweet.getDate());
                break;
            case DAILY_REPORT_TYPE:
            default:
                result = isSameDayMonthAndYear(item.getLeft(), tweet.getDate());
                break;
        }

        return result;
    }

    private boolean isSameMonthOfYear(Date a, Date b) {

        Calendar calendarA = Calendar.getInstance();
        Calendar calendarB = Calendar.getInstance();

        calendarA.setTime(a);
        calendarB.setTime(b);

        return (calendarA.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR)) && (calendarA.get(Calendar.MONTH) == calendarB.get(Calendar.MONTH));
    }

    private boolean isSameDayMonthAndYear(Date a, Date b) {

        Calendar calendarA = Calendar.getInstance();
        Calendar calendarB = Calendar.getInstance();

        calendarA.setTime(a);
        calendarB.setTime(b);

        return (calendarA.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR))
                && (calendarA.get(Calendar.MONTH) == calendarB.get(Calendar.MONTH))
                && (calendarA.get(Calendar.DATE) == calendarB.get(Calendar.DATE));
    }

    private Report findSameDateReport(Set<Report> reports, Report item) {

        for (Report report : reports) {
            if (report.getKeyword().equalsIgnoreCase(item.getKeyword())) {

                if (report.getType().equalsIgnoreCase(item.getType())) {

                    switch (report.getType()) {
                        case MONTHLY_REPORT_TYPE:

                            if (isSameMonthOfYear(report.getDateTransformed(), item.getDateTransformed())) {
                                return report;
                            }

                            break;
                        case DAILY_REPORT_TYPE:
                        default:

                            if (isSameDayMonthAndYear(report.getDateTransformed(), item.getDateTransformed())) {
                                return report;
                            }

                            break;
                    }
                }
            }
        }

        return null;
    }
}
