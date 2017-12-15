package data.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import data.streaming.utils.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * POJO to represent a tweet which is a Tuple4<Name, Text, Date, Language>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TweetDTO {
    @JsonProperty("created_at")
    @SerializedName("created_at")
    private String createdAt;

    @JsonProperty("user")
    @SerializedName("user")
    private TweetUserDTO user;

    @JsonProperty("text")
    @SerializedName("text")
    private String text;

    @JsonProperty("lang")
    @SerializedName("lang")
    private String language;

    public TweetDTO(String createdAt, TweetUserDTO user, String text, String language) throws ParseException {
        super();
        this.createdAt = createdAt;
        this.user = user;
        this.text = text;
        this.language = language;
    }

    public TweetDTO() {
        super();
    }

    public TweetUserDTO getUser() {
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
            date = Utils.getTwitterDate(this.createdAt);
        } catch (ParseException ignored) {
        }

        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TweetDTO tweetDTO = (TweetDTO) o;

        if (createdAt != null ? !createdAt.equals(tweetDTO.createdAt) : tweetDTO.createdAt != null) return false;
        if (user != null ? !user.equals(tweetDTO.user) : tweetDTO.user != null) return false;
        if (text != null ? !text.equals(tweetDTO.text) : tweetDTO.text != null) return false;
        return language != null ? language.equals(tweetDTO.language) : tweetDTO.language == null;
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
        return "TweetDTO{" +
                "createdAt='" + createdAt + '\'' +
                ", user=" + user +
                ", text='" + text + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}