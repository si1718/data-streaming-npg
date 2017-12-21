package data.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TweetUser {

    @JsonProperty("id_str")
    @SerializedName("id_str")
    private String idStr;

    @SerializedName("name")
    private String name;

    @JsonProperty("screen_name")
    @SerializedName("screen_name")
    private String screenName;

    @JsonProperty("followers_count")
    @SerializedName("followers")
    private Integer followers;

    @JsonProperty("friends_count")
    @SerializedName("friends")
    private Integer friends;

    public TweetUser(String idStr, String name, String screenName, Integer followers, Integer friends) {
        super();
        this.idStr = idStr;
        this.name = name;
        this.screenName = screenName;
        this.followers = followers;
        this.friends = friends;
    }

    public TweetUser() {
        super();
    }

    public String getIdStr() {
        return idStr;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public Integer getFollowers() {
        return followers;
    }

    public Integer getFriends() {
        return friends;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TweetUser that = (TweetUser) o;

        if (idStr != null ? !idStr.equals(that.idStr) : that.idStr != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (screenName != null ? !screenName.equals(that.screenName) : that.screenName != null) return false;
        if (followers != null ? !followers.equals(that.followers) : that.followers != null) return false;
        return friends != null ? friends.equals(that.friends) : that.friends == null;
    }

    @Override
    public int hashCode() {
        int result = idStr != null ? idStr.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (screenName != null ? screenName.hashCode() : 0);
        result = 31 * result + (followers != null ? followers.hashCode() : 0);
        result = 31 * result + (friends != null ? friends.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TweetUser{" +
                "idStr='" + idStr + '\'' +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                ", followers=" + followers +
                ", friends=" + friends +
                '}';
    }
}
