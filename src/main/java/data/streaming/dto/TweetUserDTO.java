package data.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TweetUserDTO {

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

    public TweetUserDTO(String idStr, String name, String screenName, Integer followers, Integer friends) {
        super();
        this.idStr = idStr;
        this.name = name;
        this.screenName = screenName;
        this.followers = followers;
        this.friends = friends;
    }

    public TweetUserDTO() {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((idStr == null) ? 0 : idStr.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TweetUserDTO other = (TweetUserDTO) obj;
        if (idStr == null) {
            if (other.idStr != null)
                return false;
        } else if (!idStr.equals(other.idStr))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TweetUser [idStr=" + idStr + ", name=" + name + ", screenName=" + screenName + ", followers="
                + followers + ", friends=" + friends + "]";
    }


}
