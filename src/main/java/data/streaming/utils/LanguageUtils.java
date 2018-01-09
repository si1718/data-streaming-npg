package data.streaming.utils;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
//import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;

public class LanguageUtils {

    private static final Locale SPANISH = new Locale("es", "ES");
//    private static final MaxentTagger SPANISH_TAGGER = new MaxentTagger("resources/spanish-ud.tagger");
//    private static final MaxentTagger ENGLISH_TAGGER = new MaxentTagger("resources/english-distsim.tagger");
//    private static MaxentTagger tagger;

    // MARK: Public Functions

    public static Set<String> getKeywordsFromText(String text) {

        Locale locale = getLocaleFromText(text);

        Set<String> allStopWords = new TreeSet<>();
        Set<String> spanishStopWords = getSpanishStopWords();
        Set<String> englishStopWords = getEnglishStopWords();
        allStopWords.addAll(spanishStopWords);
        allStopWords.addAll(englishStopWords);

        Set<String> keywords = new TreeSet<>();
        Map<String, Integer> words = new HashMap<>();

        final Integer MAX_KEYWORDS = 6;
        Integer totalEnglishWords = 0;
        Integer totalSpanishWords = 0;

        String[] data = text.split("[\\s-.,;:]+");

        for(String word: data) {

            if(spanishStopWords.contains(word.trim())) {
                totalSpanishWords++;
            }

            if(englishStopWords.contains(word.trim())) {
                totalEnglishWords++;
            }
        }

        Arrays.stream(data).map(String::trim).filter(x -> !allStopWords.contains(x.toLowerCase())).forEach(word -> {

            if (!words.containsKey(word)) {
                words.put(word, 1);
            } else {
                words.put(word, words.get(word) + 1);
            }
        });

//        tagger = (totalSpanishWords > totalEnglishWords) ? SPANISH_TAGGER :
//                (totalEnglishWords > totalSpanishWords) ? ENGLISH_TAGGER :
//                        (locale.equals(SPANISH)) ? SPANISH_TAGGER : ENGLISH_TAGGER;
//
//        locale = (tagger.equals(SPANISH_TAGGER)) ? SPANISH : ENGLISH;

//        Set<String> importantWords = getImportantWords(text, locale);
        Set<String> importantWords = words.keySet();

        Comparator<Map.Entry<String, Integer>> byImportance = Map.Entry.<String, Integer>comparingByValue().reversed();
        Comparator<Map.Entry<String, Integer>> byLength = (a, b) -> a.getKey().length() > b.getKey().length() ? -1 : a.getKey().length() < b.getKey().length() ? 1 : 0;

        // Get all words that begin by uppercase in repeats order with limit MAX_KEYWORDS
        words.entrySet().stream()
                .filter(x -> importantWords.contains(x.getKey().toLowerCase()))
                .sorted(byImportance.thenComparing(byLength))
                .limit(MAX_KEYWORDS)
                .forEach(x -> {

                    String key = x.getKey();
                    key = key.trim();
                    key = key.substring(0, 1).toUpperCase() + key.substring(1);

                    keywords.add(key);
                });

        return keywords;
    }

    public static Locale getLocaleFromText(String text) {

        List<LanguageProfile> languageProfiles;
        LanguageDetector languageDetector;
        TextObjectFactory textObjectFactory;
        TextObject textObject;
        com.google.common.base.Optional<LdLocale> lang;
        Locale locale = ENGLISH;

        try {

            //load all languages:
            languageProfiles = new LanguageProfileReader().readAllBuiltIn();

            //build language detector:
            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .withProfiles(languageProfiles)
                    .build();

            //create a text object factory
            textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

            //query:
            textObject = textObjectFactory.forText(text);
            lang = languageDetector.detect(textObject);

            locale = (lang.isPresent() && lang.get().getLanguage().equals(SPANISH.getLanguage())) ? SPANISH : ENGLISH;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return locale;
    }


    // MARK: Private Functions

    private static Set<String> getSpanishStopWords() {

        JSONParser parser = new JSONParser();
        Set<String> stopWords = new TreeSet<>();

        try {

            JSONArray jsonArrayES = (JSONArray) parser.parse(new FileReader("resources/stopwords-es.json"));
            stopWords.addAll(jsonArrayES);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stopWords;
    }

    private static Set<String> getEnglishStopWords() {

        JSONParser parser = new JSONParser();
        Set<String> stopWords = new TreeSet<>();

        try {

            JSONArray jsonArrayEN = (JSONArray) parser.parse(new FileReader("resources/stopwords-en.json"));
            stopWords.addAll(jsonArrayEN);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stopWords;
    }

    private static Boolean isImportantWord(String word, Locale locale) {

        Boolean isImportant;

        if(locale.equals(SPANISH)) {
            isImportant = word.contains("NOUN") || word.contains("PROPN");
        } else {
            isImportant = word.contains("NN") || word.contains("NNS") || word.contains("NNP") || word.contains("NNPS");
        }

        return isImportant;
    }

    private static Set<String> getImportantWords(String sentence, Locale locale) {

        String tagged = tagger.tagString(sentence);

        return Arrays
                .stream(tagged.split("[\\s]+"))
                .filter(word -> isImportantWord(word, locale))
                .map(x -> {
                    String clean = x.split("_")[0];
                    clean = clean.trim();
                    clean = clean.toLowerCase();
                    return clean;
                })
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
