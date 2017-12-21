package data.streaming.utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import data.streaming.db.MongoConnector;
import data.streaming.dto.Keyword;
import data.streaming.dto.Tweet;
import org.bson.Document;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.knn.user.UserUserItemScorer;

public class Utils {

    // DATES FORMATS
    public static final String TWITTER_DATE = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
    public static final String ISO_8601_DATE = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String WITHOUT_TIME_DATE = "yyyy-MM-dd";

    public static final String[] TAG_NAMES = {"#OTDirecto12D", "#InmaculadaConcepcion"};
    public static final String BASE_URL_DATABASE = "mongodb://pozas91:pozas91@ds149865.mlab.com:49865/si1718-npg-chapters";
    public static final String TWEETS_URL_DATABASE = "mongodb://pozas91:pozas91@ds161346.mlab.com:61346/si1718-npg-tweets";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Tweet createTweetDTO(String x) {

        Tweet result = null;

        try {
            result = mapper.readValue(x, Tweet.class);
        } catch (IOException ignored) {

        }

        return result;
    }

    public static Boolean isTweetValid(String x) {

        boolean result = true;

        try {
            mapper.readValue(x, Tweet.class);
        } catch (IOException ignored) {
            result = false;
        }

        return result;
    }

    public static void saveTweetInDatabase(Tweet x) {

        Gson gson = new Gson();
        String json = gson.toJson(x);
        Document tweet = Document.parse(json);

        MongoConnector mongoConnector = new MongoConnector();
        mongoConnector.populateCollection(MongoConnector.TWEETS_COLLECTION, MongoConnector.TWEETS_COLLECTION, tweet);
    }

    public static String convertDateToISO8601(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE, Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static Date getTwitterDate(String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER_DATE, Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }

    public static <T> List<List<T>> batchList(List<T> inputList, final int maxSize) {

        final int size = inputList.size();
        List<List<T>> subLists = new ArrayList<>();

        for(int i = 0; i < size; i += maxSize) {
            subLists.add(new ArrayList<>(inputList.subList(i, Math.min(size, i + maxSize))));
        }

        return subLists;
    }

    public static String getFirstLetters(String text) {

        StringBuilder firstLetters = new StringBuilder();

        text = text.replaceAll("[.,-]", ""); // Replace dots, etc (optional)

        for(String s : text.split(" ")) {

            try {
                firstLetters.append(s.charAt(0));
            } catch (Exception e) {
                System.err.println("Error: " + e.getLocalizedMessage() + ", in chartAt(0) about '" + s + "' string.");
            }
        }

        return firstLetters.toString().toLowerCase();
    }

    public static String convertDateToWithoutTime(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(WITHOUT_TIME_DATE, Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static int mapValue(int x, int in_min, int in_max, int out_min, int out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

//    public static ItemRecommender getRecommender(Set<Keyword> statistics) throws RecommenderBuildException {
//
//        LenskitConfiguration config = new LenskitConfiguration();
//        EventDAO myDAO = EventCollectionDAO.create(createEventCollection(statistics));
//
//        config.bind(EventDAO.class).to(myDAO);
//        config.bind(ItemScorer.class).to(UserUserItemScorer.class);
//
//        Recommender rec = LenskitRecommender.build(config);
//        return rec.getItemRecommender();
//    }
}
