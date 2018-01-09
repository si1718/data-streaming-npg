package data.streaming.scheduled;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import data.streaming.db.MongoConnector;
import data.streaming.dto.*;
import data.streaming.utils.Utils;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static data.streaming.db.MongoConnector.*;

class Reports {

    private final String DAILY_REPORT_TYPE = "daily_report";
    private final String MONTHLY_REPORT_TYPE = "monthly_report";
    private final String BOOKS_REPOSITORY_URL = "https://si1718-lgm-books.herokuapp.com/api/v1/books";

    private final MongoConnector mongoConnector = new MongoConnector();

    void run() {

        generateKeywordsReports();
        generateChaptersReports();
    }

    private void generateChaptersReports() {

        List<Chapter> chapters = mongoConnector.getChapters();
        List<BookExtend> books = getBooks(BOOKS_REPOSITORY_URL);
        List<Document> documents = new ArrayList<>();
        Map<Integer, Integer> researchersPerYear = new TreeMap<>();
        Map<Integer, Integer> researchersPerChapter = new TreeMap<>();

        // MARK: Researchers per Year
        chapters.stream().filter(x -> x.getBook() != null && x.getBook().getKey() != null).forEach(chapter -> {

            books.stream().filter(x -> x.getIsbn() != null && x.getYear() != null).forEach(book -> {

                String chapterBookKey = chapter.getBook().getKey().trim();
                String bookIsbn = book.getIsbn().trim();

                if(chapterBookKey.equalsIgnoreCase(bookIsbn)) {

                    if(researchersPerYear.containsKey(book.getYear())) {
                        researchersPerYear.put(book.getYear(), researchersPerYear.get(book.getYear()) + chapter.getResearchers().size());
                    } else {
                        researchersPerYear.put(book.getYear(), chapter.getResearchers().size());
                    }
                }
            });
        });

        researchersPerYear.forEach((year, researchers) -> {
            Document document = new Document();
            document.append("year", year);
            document.append("researchers", researchers);
            documents.add(document);
        });

        mongoConnector.cleanCollection(RESEARCHERS_PER_YEAR_COLLECTION);
        mongoConnector.populateCollection(RESEARCHERS_PER_YEAR_COLLECTION, documents);


        documents.clear();

        // MARK: Researchers per Chapter
        chapters.stream().filter(x -> x.getResearchers().size() > 0).forEach(chapter -> {

            Integer numberOfResearchers = chapter.getResearchers().size();

            if(researchersPerChapter.containsKey(numberOfResearchers)) {
                researchersPerChapter.put(numberOfResearchers, researchersPerChapter.get(numberOfResearchers) + 1);
            } else {
                researchersPerChapter.put(numberOfResearchers, 1);
            }
        });

        researchersPerChapter.forEach((researchers, numberOfChapters) -> {
            Document document = new Document();
            document.append("chapters", numberOfChapters);
            document.append("researchers", researchers);
            documents.add(document);
        });
        mongoConnector.cleanCollection(RESEARCHERS_PER_CHAPTER_COLLECTION);
        mongoConnector.populateCollection(RESEARCHERS_PER_CHAPTER_COLLECTION, documents);
    }

    private List<BookExtend> getBooks(String url){

        List<BookExtend> books = new ArrayList<>();
        Gson gson = new Gson();

        try {

            URL obj = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
            httpURLConnection.setRequestMethod("GET");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            books = gson.fromJson(bufferedReader, new TypeToken<List<BookExtend>>(){}.getType());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    private void generateKeywordsReports() {

        Map<String, List<CustomTuple<Date, Integer>>> dailyReport = new HashMap<>();
        Map<String, List<CustomTuple<Date, Integer>>> monthlyReport = new HashMap<>();
        List<Document> documents = new ArrayList<>();
        SortedSet<Report> newReports = new TreeSet<>();

        System.out.printf("%n Executing reports task... %n%n");

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

        mongoConnector.cleanCollection(REPORTS_COLLECTION);
        mongoConnector.cleanCollection(TWEETS_DATABASE, TWEETS_COLLECTION);

        mongoConnector.populateCollection(REPORTS_COLLECTION, documents);

        System.out.printf("%n Reports task finished. %n%n");
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
