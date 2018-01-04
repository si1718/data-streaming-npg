package data.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import data.streaming.utils.Utils;

import java.text.ParseException;
import java.util.Date;

/**
 * POJO to represent a tweet which is a Tuple4<Name, Text, Date, LanguageUtils>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {
    @JsonProperty("created_at")
    @SerializedName("created_at")
    private String createdAt;

    @JsonProperty("user")
    @SerializedName("user")
    private TweetUser user;

    @JsonProperty("text")
    @SerializedName("text")
    private String text;

    @JsonProperty("lang")
    @SerializedName("lang")
    private String language;

    public Tweet(String createdAt, TweetUser user, String text, String language) throws ParseException {
        super();
        this.createdAt = createdAt;
        this.user = user;
        this.text = text;
        this.language = language;
    }

    public Tweet() {
        super();
    }

    public TweetUser getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLanguage() {
        return language;
    }

    public Date getDate() {

        Date date = null;

        try {
            date = Utils.convertTwitterDateToDate(this.createdAt);
        } catch (ParseException ignored) {
        }

        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tweet tweet = (Tweet) o;

        if (createdAt != null ? !createdAt.equals(tweet.createdAt) : tweet.createdAt != null) return false;
        if (user != null ? !user.equals(tweet.user) : tweet.user != null) return false;
        if (text != null ? !text.equals(tweet.text) : tweet.text != null) return false;
        return language != null ? language.equals(tweet.language) : tweet.language == null;
    }

    @Override
    public int hashCode() {
        int result = createdAt != null ? createdAt.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "createdAt='" + createdAt + '\'' +
                ", user=" + user +
                ", text='" + text + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}