package data.streaming.utils;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import data.streaming.db.MongoConnector;
import data.streaming.dto.Rating;
import data.streaming.dto.Tweet;
import org.apache.flink.shaded.com.google.common.collect.Maps;
import org.bson.Document;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer;
import org.grouplens.lenskit.baseline.UserMeanBaseline;
import org.grouplens.lenskit.baseline.UserMeanItemScorer;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.knn.user.UserUserItemScorer;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;

public class Utils {

    // DATES FORMATS
    public static final String TWITTER_DATE = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
    public static final String ISO_8601_DATE = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String WITHOUT_TIME_DATE = "yyyy-MM-dd";

    // URLS
    public static final String BASE_URL_DATABASE = "mongodb://pozas91:pozas91@ds149865.mlab.com:49865/si1718-npg-chapters";
    public static final String TWEETS_URL_DATABASE = "mongodb://pozas91:pozas91@ds161346.mlab.com:61346/si1718-npg-tweets";

    // MAPPER
    private static final ObjectMapper mapper = new ObjectMapper();

    // RESTRICTIONS
    private static final int MAX_RECOMMENDATIONS = 6;

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

    public static Date convertISO8601ToDate(String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(ISO_8601_DATE, Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }

    public static Date convertTwitterDateToDate(String date) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER_DATE, Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }

    public static <T> List<List<T>> batchList(List<T> inputList, final Integer maxSize) {

        final Integer size = inputList.size();
        List<List<T>> subLists = new ArrayList<>();

        for(Integer i = 0; i < size; i += maxSize) {
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

    public static Integer mapValue(Integer x, Integer inMin, Integer inMax, Integer outMin, int outMax) {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    public static ItemRecommender getRecommender(Set<Rating> ratings) throws RecommenderBuildException {

        LenskitConfiguration config = new LenskitConfiguration();
        EventDAO myDAO = EventCollectionDAO.create(createEventCollection(ratings));

        config.bind(EventDAO.class).to(myDAO);
        config.bind(ItemScorer.class).to(UserUserItemScorer.class);

        Recommender recommender = LenskitRecommender.build(config);
        return recommender.getItemRecommender();
    }

    private static Collection<? extends Event> createEventCollection(Set<Rating> ratings) {

        List<Event> result = new LinkedList<>();

        ratings.forEach(rating -> {
            MutableRating mutableRating = new MutableRating();
            mutableRating.setUserId(rating.getChapterA().hashCode());
            mutableRating.setItemId(rating.getChapterB().hashCode());
            mutableRating.setRating(rating.getRating());
            result.add(mutableRating);
        });

        return result;
    }

    public static Map<String, Set<String>> getRecommendations(ItemRecommender recommender, Set<Rating> ratings) throws IOException {

        Map<String, Set<String>> information = new HashMap<>();

        Map<String, Long> keys = Maps.asMap(ratings.stream().map(Rating::getChapterA)
                .collect(Collectors.toSet()), (String y) -> (long) y.hashCode());

        Map<Long, List<String>> reverse = ratings.stream().map(Rating::getChapterA)
                .collect(Collectors.groupingBy((String x) -> (long) x.hashCode()));

        for (String key : keys.keySet()) {

            List<ScoredId> recommendations = recommender.recommend(keys.get(key), MAX_RECOMMENDATIONS);

            if (recommendations.size() > 0) {
                information.put(key, recommendations.stream().map(x -> reverse.get(x.getId()).get(0)).collect(Collectors.toSet()));
            }
        }

        return information;
    }

    public static Set<Rating> getRatings() throws IOException {
        Set<Rating> result = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader("out/data.csv"));
        String line = reader.readLine();
        while (line != null) {
            String[] splits = line.split(",");
            if (splits.length == 3) {
                result.add(new Rating(splits[0], splits[1], Double.valueOf(splits[2])));
            }
            line = reader.readLine();
        }

        reader.close();
        return result;
    }
}
