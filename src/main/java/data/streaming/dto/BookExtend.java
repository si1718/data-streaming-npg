package data.streaming.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookExtend {

    @SerializedName("author")
    private List<String> authors;

    @SerializedName("title")
    private String title;

    @SerializedName("year")
    private Integer year;

    @SerializedName("isbn")
    private String isbn;

    public BookExtend(List<String> authors, String title, Integer year, String isbn) {
        this.authors = authors;
        this.title = title;
        this.year = year;
        this.isbn = isbn;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BookExtend that = (BookExtend) o;

        if (authors != null ? !authors.equals(that.authors) : that.authors != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (year != null ? !year.equals(that.year) : that.year != null) return false;
        return isbn != null ? isbn.equals(that.isbn) : that.isbn == null;
    }

    @Override
    public int hashCode() {
        int result = authors != null ? authors.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (year != null ? year.hashCode() : 0);
        result = 31 * result + (isbn != null ? isbn.hashCode() : 0);
        return result;
    }
}
