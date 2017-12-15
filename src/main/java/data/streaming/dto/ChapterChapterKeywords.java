package data.streaming.dto;

import java.util.SortedSet;

public class ChapterChapterKeywords {

    private Chapter chapterA;
    private Chapter chapterB;
    private SortedSet<String> keywords;

    public ChapterChapterKeywords(Chapter chapterA, Chapter chapterB, SortedSet<String> keywords) {
        this.chapterA = chapterA;
        this.chapterB = chapterB;
        this.keywords = keywords;
    }

    public Chapter getChapterA() {
        return chapterA;
    }

    public void setChapterA(Chapter chapterA) {
        this.chapterA = chapterA;
    }

    public Chapter getChapterB() {
        return chapterB;
    }

    public void setChapterB(Chapter chapterB) {
        this.chapterB = chapterB;
    }

    public SortedSet<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(SortedSet<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChapterChapterKeywords that = (ChapterChapterKeywords) o;

        if (chapterA != null ? !chapterA.equals(that.chapterA) : that.chapterA != null) return false;
        if (chapterB != null ? !chapterB.equals(that.chapterB) : that.chapterB != null) return false;
        return keywords != null ? keywords.equals(that.keywords) : that.keywords == null;
    }

    @Override
    public int hashCode() {
        int result = chapterA != null ? chapterA.hashCode() : 0;
        result = 31 * result + (chapterB != null ? chapterB.hashCode() : 0);
        result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
        return result;
    }
}
