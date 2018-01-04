package data.streaming.dto;

import com.google.gson.annotations.SerializedName;
import data.streaming.utils.Utils;
import org.bson.Document;

import java.text.ParseException;
import java.util.Date;

public class Report implements Comparable {

    @SerializedName("keyword")
    private String keyword;

    @SerializedName("date")
    private String date;

    @SerializedName("count")
    private Integer count;

    @SerializedName("type")
    private String type;

    public Report(String keyword, String date, Integer count, String type) {
        this.keyword = keyword;
        this.date = date;
        this.count = count;
        this.type = type;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDateTransformed() {

        Date date = null;

        try {
            date = Utils.convertISO8601ToDate(this.date);
        } catch (ParseException ignored) {
        }

        return date;
    }

    public Document toDocument() {

        Document document = new Document();
        document.append("keyword", keyword);
        document.append("date", date);
        document.append("count", count);
        document.append("type", type);

        return document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Report report = (Report) o;

        if (keyword != null ? !keyword.equals(report.keyword) : report.keyword != null) return false;
        if (date != null ? !date.equals(report.date) : report.date != null) return false;
        if (count != null ? !count.equals(report.count) : report.count != null) return false;
        return type != null ? type.equals(report.type) : report.type == null;
    }

    @Override
    public int hashCode() {
        int result = keyword != null ? keyword.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object o) {

        if(!(o instanceof Report)) {
            return -1;
        }

        Report other = (Report) o;

        int compare = this.keyword.compareToIgnoreCase(other.getKeyword());

        if(compare == 0) {
            compare = this.getDateTransformed().compareTo(other.getDateTransformed());
        }

        if(compare == 0) {
            compare = this.type.compareTo(other.type);
        }

        if(compare == 0) {
            compare = this.count.compareTo(other.count);
        }

        return compare;
    }
}
