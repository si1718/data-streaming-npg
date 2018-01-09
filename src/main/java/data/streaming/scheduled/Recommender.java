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
import java.util.stream.Collectors;

import static data.streaming.db.MongoConnector.CHAPTERS_COLLECTION;
import static data.streaming.db.MongoConnector.RECOMMENDATIONS_COLLECTION;

public class Recommender {

    private final MongoConnector mongoConnector = new MongoConnector();

    public void run() {

//        generateChaptersWithKeywords();
        generateRecommendations();
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

        mongoConnector.cleanCollection(RECOMMENDATIONS_COLLECTION);
        mongoConnector.populateCollection(RECOMMENDATIONS_COLLECTION, documents);
    }

    private void generateChaptersWithKeywords() {

        System.out.printf("%n Initializing generate chapters with keywords... %n%n");

        List<Chapter> chapters = mongoConnector.getChapters();
        List<Document> documents = new ArrayList<>();
        Gson gson = new Gson();

        // Only 2% of chapters will have keywords
        final Integer SAMPLE = (chapters.size() * 30) / 100;

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
