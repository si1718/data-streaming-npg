package data.streaming.dto;

import com.google.gson.annotations.SerializedName;

public class Rating {

    @SerializedName("chapter_a")
    private String chapterA;

    @SerializedName("chapter_b")
    private String chapterB;

    @SerializedName("rating")
    private Double rating;

    public Rating(String chapterA, String chapterB, Double rating) {
        super();
        this.chapterA = chapterA;
        this.chapterB = chapterB;
        this.rating = rating;
    }

    public String getChapterA() {
        return chapterA;
    }

    public void setChapterA(String key) {
        this.chapterA = key;
    }

    public String getChapterB() {
        return chapterB;
    }

    public void setChapterB(String chapterB) {
        this.chapterB = chapterB;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rating that = (Rating) o;

        if (chapterA != null ? !chapterA.equals(that.chapterA) : that.chapterA != null) return false;
        if (chapterB != null ? !chapterB.equals(that.chapterB) : that.chapterB != null) return false;
        return rating != null ? rating.equals(that.rating) : that.rating == null;
    }

    @Override
    public int hashCode() {
        int result = chapterA != null ? chapterA.hashCode() : 0;
        result = 31 * result + (chapterB != null ? chapterB.hashCode() : 0);
        result = 31 * result + (rating != null ? rating.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "chapterA='" + chapterA + '\'' +
                ", chapterB='" + chapterB + '\'' +
                ", rating=" + rating +
                '}';
    }
}
