package data.streaming.db;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import data.streaming.dto.Chapter;
import data.streaming.dto.TweetDTO;
import data.streaming.utils.Utils;
import org.bson.Document;

import java.util.*;

public class MongoConnection {

    private final MongoClientURI uri = new MongoClientURI(Utils.URL_DATABASE);
    private MongoClient client;
    private MongoDatabase database;

    public static final String TWEETS_COLLECTION = "tweets";
    public static final String CHAPTERS_COLLECTION = "chapters";
    public static final String KEYWORD_DATE_TWEETS_COLLECTION = "keyword_date_tweets";
    public static final String CHAPTERS_RATING_COLLECTION = "chapters_rating";

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

    public Map<String, List<TweetDTO>> getTweetsByKeywordAndDate(String keyword) {

        openDatabase();

        Map<String, List<TweetDTO>> information = new HashMap<>();
        List<TweetDTO> tweets = new ArrayList<>();

        MongoCollection<Document> collection = this.getCollection(TWEETS_COLLECTION);

        List<Document> documents = collection.find(Filters.regex("text", ".*" + keyword + ".*", "i")).into(new ArrayList<>());

        Gson gson = new Gson();

        documents.forEach(document -> {

            String json = document.toJson();
            TweetDTO tweetDTO = gson.fromJson(json, TweetDTO.class);
            tweets.add(tweetDTO);
        });

        closeDatabase();

        tweets.forEach(tweetDTO -> {

            Date date = tweetDTO.getDate();

            if(date != null) {

                String dateWithoutTime = Utils.convertDateToWithoutTime(date);

                if(information.containsKey(dateWithoutTime)) {

                    information.get(dateWithoutTime).add(tweetDTO);

                } else {

                    List<TweetDTO> simple = new ArrayList<>();
                    simple.add(tweetDTO);
                    information.put(dateWithoutTime, simple);
                }
            }
        });

        return information;
    }

    public void cleanCollection(String collection) {

        openDatabase();

        this.getCollection(collection).drop();

        closeDatabase();
    }

    public SortedSet<String> getKeywords() {

        openDatabase();

        MongoCollection<Document> collection = this.getCollection(CHAPTERS_COLLECTION);
        SortedSet<String> keywords = collection.distinct("keywords", String.class).into(new TreeSet<>());

        closeDatabase();

        return keywords;
    }

    private void openDatabase() {
        this.client = new MongoClient(uri);
        this.database = this.client.getDatabase(uri.getDatabase());
    }

    private void closeDatabase() {
        this.client.close();
    }

    private MongoCollection<Document> getCollection(String collection) {
        return this.database.getCollection(collection);
    }

    public List<Chapter> getChapters() {

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
}
