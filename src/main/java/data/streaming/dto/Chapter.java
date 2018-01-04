package data.streaming.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.SortedSet;

public class Chapter {

    @SerializedName("idChapter")
    private String idChapter;

    @SerializedName("book")
    private Book book;

    @SerializedName("name")
    private String name;

    @SerializedName("researchers")
    private List<Researcher> researchers;

    @SerializedName("pages")
    private String pages;

    @SerializedName("viewURL")
    private String viewURL;

    @SerializedName("keywords")
    private SortedSet<String> keywords;

    public Chapter(String idChapter, Book book, String name, List<Researcher> researchers, String pages, String viewURL, SortedSet<String> keywords) {
        this.idChapter = idChapter;
        this.book = book;
        this.name = name;
        this.researchers = researchers;
        this.pages = pages;
        this.viewURL = viewURL;
        this.keywords = keywords;
    }

    public String getIdChapter() {
        return idChapter;
    }

    public void setIdChapter(String idChapter) {
        this.idChapter = idChapter;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Researcher> getResearchers() {
        return researchers;
    }

    public void setResearchers(List<Researcher> researchers) {
        this.researchers = researchers;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getViewURL() {
        return viewURL;
    }

    public void setViewURL(String viewURL) {
        this.viewURL = viewURL;
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

        Chapter chapter = (Chapter) o;

        if (idChapter != null ? !idChapter.equals(chapter.idChapter) : chapter.idChapter != null) return false;
        if (book != null ? !book.equals(chapter.book) : chapter.book != null) return false;
        if (name != null ? !name.equals(chapter.name) : chapter.name != null) return false;
        if (researchers != null ? !researchers.equals(chapter.researchers) : chapter.researchers != null) return false;
        if (pages != null ? !pages.equals(chapter.pages) : chapter.pages != null) return false;
        if (viewURL != null ? !viewURL.equals(chapter.viewURL) : chapter.viewURL != null) return false;
        return keywords != null ? keywords.equals(chapter.keywords) : chapter.keywords == null;
    }

    @Override
    public int hashCode() {
        int result = idChapter != null ? idChapter.hashCode() : 0;
        result = 31 * result + (book != null ? book.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (researchers != null ? researchers.hashCode() : 0);
        result = 31 * result + (pages != null ? pages.hashCode() : 0);
        result = 31 * result + (viewURL != null ? viewURL.hashCode() : 0);
        result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
        return result;
    }
}
