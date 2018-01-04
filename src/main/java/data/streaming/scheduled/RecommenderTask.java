package data.streaming.scheduled;

import com.google.gson.Gson;
import data.streaming.db.MongoConnector;
import data.streaming.dto.Chapter;
import data.streaming.dto.Rating;
import data.streaming.utils.LanguageUtils;
import data.streaming.utils.Utils;
import org.bson.Document;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RecommenderBuildException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static data.streaming.db.MongoConnector.CHAPTERS_COLLECTION;
import static data.streaming.db.MongoConnector.RECOMMENDATIONS_COLLECTION;
import static java.util.concurrent.TimeUnit.SECONDS;

public class RecommenderTask {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Integer INITIAL_DELAY = 0; // In seconds
    private final Integer PERIOD = 60 * 60 * 24; // In seconds => 1 day
    private final Integer SECONDS_TO_CANCEL = 60 * 60; // In seconds

    private final MongoConnector mongoConnector = new MongoConnector();

    public void prepare() {

        final Runnable scheduled = this::start;

        final ScheduledFuture<?> scheduledHandle = scheduler.scheduleAtFixedRate(scheduled, INITIAL_DELAY, PERIOD, SECONDS);

//        scheduler.schedule((Runnable) () -> scheduledHandle.cancel(true), SECONDS_TO_CANCEL, SECONDS);
    }

    private void start() {

        System.out.printf("%n Initializing batch... %n%n");

        generateChaptersWithKeywords();

//        generateRecommendations();

        System.out.printf("%n Batch finished. %n%n");
    }

    private void generateRecommendations() {

        Map<String, Set<String>> recommendationsRaw = null;
        List<Document> documents = new ArrayList<>();

        Set<Rating> ratings = mongoConnector.getRatings();
        ItemRecommender recommender;

        try {
            recommender = Utils.getRecommender(ratings);
            recommendationsRaw = Utils.getRecommendations(recommender, ratings);
        } catch (RecommenderBuildException | IOException e) {
            e.printStackTrace();
        }

        assert recommendationsRaw != null;

        recommendationsRaw.forEach((key, values) -> {
            values.remove(key);

            if(values.size() > 0) {

                Document document = new Document();
                document.append("key", key);
                document.append("recommendations", values);
                documents.add(document);
            }
        });

        System.out.printf("%n Cleaning previous data in collection %s ... %n%n", RECOMMENDATIONS_COLLECTION);
        mongoConnector.cleanCollection(RECOMMENDATIONS_COLLECTION);
        System.out.printf("%n Data cleaned. %n");

        System.out.printf("%n Saving recommendations... %n%n");
        mongoConnector.populateCollection(RECOMMENDATIONS_COLLECTION, documents);
        System.out.printf("%n Recommendations saved. %n%n");
    }

    private void generateChaptersWithKeywords() {

        System.out.printf("%n Initializing generate chapters with keywords... %n%n");

        List<Chapter> chapters = mongoConnector.getChapters();
        List<Document> documents = new ArrayList<>();
        Gson gson = new Gson();

        // Only 2% of chapters will have keywords
        final Integer SAMPLE = (chapters.size() * 2) / 100;

        List<Chapter> withKeywords = chapters.subList(0, SAMPLE);
        List<Chapter> withoutKeywords = chapters.subList(SAMPLE, chapters.size());

        withKeywords.forEach((Chapter x) -> {

            SortedSet<String> keywords = LanguageUtils
                    .getKeywordsFromText(x.getName())
                    .stream()
                    .filter(keyword -> keyword.length() >= 3)
                    .collect(Collectors.toCollection(TreeSet::new));

            x.setKeywords(keywords);
        });

        withoutKeywords.forEach(x -> x.setKeywords(null));

        mongoConnector.cleanCollection(CHAPTERS_COLLECTION);

        chapters.forEach(x -> {
            String json = gson.toJson(x);
            documents.add(Document.parse(json));
        });

        mongoConnector.populateCollection(CHAPTERS_COLLECTION, documents);

        System.out.printf("%n Finishing generate chapters with keywords... %n%n");
    }
}
