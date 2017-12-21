package data.streaming.dto;

import com.google.gson.annotations.SerializedName;

public class Keyword {

    @SerializedName("key1")
    private String key1;

    @SerializedName("key2")
    private String key2;

    @SerializedName("statistic")
    private Double statistic;

    public Keyword(String key1, String key2, Double statistic) {
        super();
        this.key1 = key1;
        this.key2 = key2;
        this.statistic = statistic;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key) {
        this.key1 = key;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public Double getStatistic() {
        return statistic;
    }

    public void setStatistic(Double statistic) {
        this.statistic = statistic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Keyword that = (Keyword) o;

        if (key1 != null ? !key1.equals(that.key1) : that.key1 != null) return false;
        if (key2 != null ? !key2.equals(that.key2) : that.key2 != null) return false;
        return statistic != null ? statistic.equals(that.statistic) : that.statistic == null;
    }

    @Override
    public int hashCode() {
        int result = key1 != null ? key1.hashCode() : 0;
        result = 31 * result + (key2 != null ? key2.hashCode() : 0);
        result = 31 * result + (statistic != null ? statistic.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Keyword{" +
                "key1='" + key1 + '\'' +
                ", key2='" + key2 + '\'' +
                ", statistic=" + statistic +
                '}';
    }
}
