package data.streaming.dto;

import com.google.gson.annotations.SerializedName;

import java.util.SortedSet;

public class Chapter {

    @SerializedName("idChapter")
    private String idChapter;

    @SerializedName("keywords")
    private SortedSet<String> keywords;

    public Chapter(String idChapter, SortedSet<String> keywords) {
        this.idChapter = idChapter;
        this.keywords = keywords;
    }

    public String getIdChapter() {
        return idChapter;
    }

    public void setIdChapter(String idChapter) {
        this.idChapter = idChapter;
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

        Chapter that = (Chapter) o;

        if (idChapter != null ? !idChapter.equals(that.idChapter) : that.idChapter != null) return false;
        return keywords != null ? keywords.equals(that.keywords) : that.keywords == null;
    }

    @Override
    public int hashCode() {
        int result = idChapter != null ? idChapter.hashCode() : 0;
        result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
        return result;
    }
}
