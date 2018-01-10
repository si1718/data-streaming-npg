package data.streaming.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import data.streaming.dto.*;
import data.streaming.utils.Utils;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.print.Doc;
import java.util.*;
import java.util.stream.Collectors;

public class MongoConnector {

    // MARK: Mongo data
    private final MongoClientURI baseURI = new MongoClientURI(Utils.BASE_URL_DATABASE);
    private final MongoClientURI tweetsURI = new MongoClientURI(Utils.TWEETS_URL_DATABASE);
    private MongoClient client;
    private MongoDatabase database;

    // MARK: Databases
    public static final String TWEETS_DATABASE = "tweets";
    public static final String BASE_DATABASE = "base";
    private static final Integer MAX_BATCH_TO_SAVE = 500;
    private static final Integer MAX_BATCH_TO_GET = 1000;

    // MARK: Collections
    public static final String TWEETS_COLLECTION = "tweets";
    public static final String RESEARCHERS_PER_YEAR_COLLECTION = "researchers_per_year";
    public static final String RESEARCHERS_PER_CHAPTER_COLLECTION = "researchers_per_chapter";
    public static final String RECOMMENDATIONS_COLLECTION = "recommendations";
    public static final String CHAPTERS_COLLECTION = "chapters";
    public static final String REPORTS_COLLECTION = "reports";
    public static final String RATINGS_COLLECTION = "ratings";

    // MARK: Functions

    public void populateCollection(String collection, List<Document> data) {

        openDatabase();

        List<List<Document>> batches = Utils.batchList(data, MAX_BATCH_TO_SAVE);

        batches.forEach(batch -> {
            System.out.printf("%nSaving batch: %s %n%n", batch.toString());
            this.getCollection(collection).insertMany(batch);
            System.out.println("Batch saved.");
        });

        closeDatabase();
    }

    public void populateCollection(String collection, Document data) {

        openDatabase();

        this.getCollection(collection).insertOne(data);

        closeDatabase();
    }

    public void populateCollection(String database, String collection, Document data) {

        MongoClientURI uri = (database != null && database.equals(MongoConnector.TWEETS_DATABASE)) ? tweetsURI : baseURI;

        openDatabase(uri);

        this.getCollection(collection).insertOne(data);

        closeDatabase();
    }

    public List<Tweet> getTweetsByKeyword(String keyword) {

        List<Tweet> tweets = new ArrayList<>();
        Gson gson = new Gson();
        Bson query = Filters.regex("text", ".*" + keyword + ".*", "i");

        List<Document> documents = getDocumentsFromCollection(TWEETS_COLLECTION, query, tweetsURI);

        documents.forEach(document -> {

            String json = document.toJson();
            Tweet tweet = gson.fromJson(json, Tweet.class);
            tweets.add(tweet);
        });

        return tweets;
    }

    public List<Chapter> getChapters() {

        List<Chapter> chapters = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder().serializeNulls();
        Gson gson = builder.create();

        List<Document> documents = getDocumentsFromCollection(CHAPTERS_COLLECTION, null, null);

        documents.forEach(document -> {

            try {

                Chapter chapter = gson.fromJson(document.toJson(), Chapter.class);
                chapters.add(chapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return chapters;
    }

    public SortedSet<String> getKeywords() {

        openDatabase();

        MongoCollection<Document> collection = this.getCollection(CHAPTERS_COLLECTION);
        SortedSet<String> keywords = collection.distinct("keywords", String.class).into(new TreeSet<>());

        closeDatabase();

        return keywords;
    }

    public SortedSet<String> getKeywordsHashTags() {

        return getKeywords()
                .stream()
                .map(x -> "#" + x)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public List<Chapter> getChaptersWithKeywords() {

        List<Chapter> chapters = new ArrayList<>();
        Gson gson = new Gson();
        Bson query = Filters.exists("keywords", true);

        List<Document> documents = getDocumentsFromCollection(CHAPTERS_COLLECTION, query, null);

        documents.forEach(document -> {

            try {
                String json = document.toJson();
                Chapter chapter = gson.fromJson(json, Chapter.class);

                if(chapter.getKeywords().size() > 0) {
                    chapters.add(chapter);
                }

            } catch (Exception ignored) {

            }
        });

        return chapters;
    }

    public SortedSet<Report> getReports() {

        SortedSet<Report> reports = new TreeSet<>();
        Gson gson = new Gson();

        List<Document> documents = getDocumentsFromCollection(REPORTS_COLLECTION, null, null);

        documents.forEach(document -> {

            try {

                String json = document.toJson();
                Report report = gson.fromJson(json, Report.class);
                reports.add(report);

            } catch (Exception ignored) {
            }
        });

        return reports;
    }

    public Set<Rating> getRatings() {

        Set<Rating> ratings = new HashSet<>();
        Gson gson = new Gson();

        List<Document> documents = getDocumentsFromCollection(RATINGS_COLLECTION, null, null);

        documents.forEach(document -> {

            try {
                Rating rating = gson.fromJson(document.toJson(), Rating.class);
                ratings.add(rating);
            } catch (Exception ignored) {

            }
        });

        return ratings;
    }


    // MARK: Aux functions

    public void cleanCollection(String collection) {
        cleanCollection(baseURI, collection, null);
    }

    public void cleanCollection(String collection, Bson query) {
        cleanCollection(baseURI, collection, query);
    }

    public void cleanCollection(String database, String collection) {

        MongoClientURI uri;

        switch (database) {
            case TWEETS_DATABASE:
                uri = tweetsURI;
                break;
            default:
                uri = baseURI;
                break;
        }

        cleanCollection(uri, collection, null);
    }

    public void cleanCollection(MongoClientURI uri, String collection, Bson query) {

        System.out.printf("%n Cleaning previous data in collection %s ... %n%n", collection);

        openDatabase(uri);

        if(query == null) {
            this.getCollection(collection).drop();
        } else {
            this.getCollection(collection).deleteMany(query);
        }

        closeDatabase();

        System.out.printf("%n Data cleaned... %n%n");
    }


    // MARK: Private functions

    private List<Document> getDocumentsFromCollection(String collectionName, Bson query, MongoClientURI uri) {

        BasicDBObject sort = new BasicDBObject("_id", -1);
        List<Document> documents = new ArrayList<>();
        int limit;

        openDatabase((uri == null) ? baseURI : uri);

        MongoCollection<Document> collection = this.getCollection(collectionName);
        final int total = Math.toIntExact((query == null) ? collection.count() : collection.count(query));

        System.out.printf("%nGetting data from collection '%s':%n", collectionName);

        for(int i = 0; i < total; i += MAX_BATCH_TO_GET) {

            limit = ((i + MAX_BATCH_TO_GET) < total) ? MAX_BATCH_TO_GET : (total - i);

            System.out.printf("%8d total registers.%n", total);
            System.out.printf("%8d skipped registers.%n", i);
            System.out.printf("%8d got registers.%n%n", limit);
            List<Document> batch;

            if(query == null) {
                batch = collection.find().sort(sort).skip(i).limit(limit).into(new ArrayList<>());
            } else {
                batch = collection.find(query).sort(sort).skip(i).limit(limit).into(new ArrayList<>());
            }

            documents.addAll(batch);
        }

        System.out.println("Data got.");

        closeDatabase();

        return documents;
    }

    private MongoCollection<Document> getCollection(String collection) {
        return this.database.getCollection(collection);
    }

    private void openDatabase() {
        this.client = new MongoClient(baseURI);
        this.database = this.client.getDatabase(baseURI.getDatabase());
    }

    private void openDatabase(MongoClientURI uri) {
        this.client = new MongoClient(uri);
        this.database = this.client.getDatabase(uri.getDatabase());
    }

    private void closeDatabase() {
        this.client.close();
    }
}
