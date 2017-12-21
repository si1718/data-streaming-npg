package data.streaming.db;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import data.streaming.dto.Chapter;
import data.streaming.dto.Tweet;
import data.streaming.utils.Utils;
import org.bson.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MongoConnector {

    private final MongoClientURI baseURI = new MongoClientURI(Utils.BASE_URL_DATABASE);
    private final MongoClientURI tweetsURI = new MongoClientURI(Utils.TWEETS_URL_DATABASE);
    private MongoClient client;
    private MongoDatabase database;

    // MARK: Databases
    public static final String TWEETS_DATABASE = "tweets";
    public static final String BASE_DATABASE = "base";

    // MARK: Collections
    public static final String TWEETS_COLLECTION = "tweets";
    public static final String CHAPTERS_COLLECTION = "chapters";
    public static final String REPORTS_COLLECTION = "reports";
    public static final String RATINGS_COLLECTION = "ratings";

    // MARK: Functions

    public void populateCollection(String collection, List<Document> data) {

        openDatabase();

        this.getCollection(collection).insertMany(data);

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

        openDatabase(tweetsURI);

        MongoCollection<Document> collection = this.getCollection(TWEETS_COLLECTION);

        List<Document> documents = collection.find(Filters.regex("text", ".*" + keyword + ".*", "i")).into(new ArrayList<>());

        closeDatabase();

        documents.forEach(document -> {

            String json = document.toJson();
            Tweet tweet = gson.fromJson(json, Tweet.class);
            tweets.add(tweet);
        });

        return tweets;
    }

    public SortedSet<String> getKeywords() {

        openDatabase();

        MongoCollection<Document> collection = this.getCollection(CHAPTERS_COLLECTION);
        SortedSet<String> keywords = collection.distinct("keywords", String.class).into(new TreeSet<>());

        closeDatabase();

        return keywords;
    }

    public List<Chapter> getChaptersWithKeywords() {

        openDatabase();

        List<Chapter> chapters = new ArrayList<>();

        MongoCollection<Document> collection = this.getCollection(CHAPTERS_COLLECTION);
        List<Document> result = collection.find(Filters.exists("keywords", true)).into(new ArrayList<>());

        closeDatabase();

        Gson gson = new Gson();

        result.forEach(document -> {

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


    // MARK: Aux functions

    public void cleanCollection(String collection) {

        openDatabase();

        this.getCollection(collection).drop();

        closeDatabase();
    }

    public void cleanCollection(MongoClientURI uri, String collection) {

        openDatabase(uri);

        this.getCollection(collection).drop();

        closeDatabase();
    }


    // MARK: Private functions

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
