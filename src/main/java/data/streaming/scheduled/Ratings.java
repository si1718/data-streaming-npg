package data.streaming.scheduled;

import data.streaming.db.MongoConnector;
import data.streaming.dto.Chapter;
import data.streaming.dto.CustomTriple;
import data.streaming.dto.Rating;
import data.streaming.utils.Utils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static data.streaming.db.MongoConnector.RATINGS_COLLECTION;

public class Ratings {

    private final Integer MIN_IN_RATING = 0;
    private final Integer MIN_OUT_RATING = 1;
    private final Integer MAX_OUT_RATING = 5;

    private final MongoConnector mongoConnector = new MongoConnector();

    public void run() {

        saveRatings();
    }

    public void saveRatings() {

        List<Document> documents = new ArrayList<>();
        List<Rating> ratings = new ArrayList<>();
        List<CustomTriple<String, String, Integer>> tupleList = new ArrayList<>();
        List<Chapter> chapters;
        final Integer MAX_RATINGS = 800 * 1000;
        Integer max;

        System.out.printf("%n Executing ratings task... %n%n");

        chapters = mongoConnector.getChaptersWithKeywords();

        for (Chapter chapterA : chapters) {

            System.out.println("Getting data for chapter: " + chapterA.toString());

            max = Integer.MIN_VALUE;

            for (Chapter chapterB : chapters) {

                if (!chapterA.equals(chapterB)) {

                    SortedSet<String> intersection = new TreeSet<>(chapterA.getKeywords()); // use the copy constructor
                    intersection.retainAll(chapterB.getKeywords());

                    if(intersection.size() > 0) {

                        CustomTriple<String, String, Integer> tuple = new CustomTriple<>(chapterA.getIdChapter(), chapterB.getIdChapter(), intersection.size());
                        tupleList.add(tuple);

                        max = Math.max(max, intersection.size());
                    }
                }
            }

            // If max is equals to min_in_rating meanings that any chapter has any keyword in common with other chapters
            if (max > MIN_IN_RATING) {

                for (CustomTriple<String, String, Integer> tuple : tupleList) {

                    Double score = Utils.mapValue(tuple.getRight(), MIN_IN_RATING, max, MIN_OUT_RATING, MAX_OUT_RATING).doubleValue();
                    Rating rating = new Rating(tuple.getLeft(), tuple.getMiddle(), score);
                    ratings.add(rating);
                }
            }

            tupleList.clear();
        }

        ratings.subList(0, (ratings.size() > MAX_RATINGS) ? MAX_RATINGS : ratings.size()).forEach(x -> {
            Document document = new Document();
            document.append("chapter_a", x.getChapterA());
            document.append("chapter_b", x.getChapterB());
            document.append("rating", x.getRating());
            documents.add(document);
        });

        mongoConnector.cleanCollection(RATINGS_COLLECTION);
        mongoConnector.populateCollection(RATINGS_COLLECTION, documents);

        System.out.printf("%n Ratings task finished. %n%n");
    }
}
