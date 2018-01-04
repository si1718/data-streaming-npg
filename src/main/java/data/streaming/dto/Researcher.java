package data.streaming.dto;

import com.google.gson.annotations.SerializedName;

public class Researcher implements Comparable {

    @SerializedName("key")
    private String key;

    @SerializedName("name")
    private String name;

    @SerializedName("view")
    private String view;

    public Researcher(String key, String name, String view) {
        this.key = key;
        this.name = name;
        this.view = view;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Researcher that = (Researcher) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return view != null ? view.equals(that.view) : that.view == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (view != null ? view.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object o) {

        if(!(o instanceof Researcher)) {
            return -1;
        }

        Researcher other = (Researcher) o;

        int compare = this.key.compareToIgnoreCase(other.key);

        if(compare == 0) {
            compare = this.name.compareToIgnoreCase(other.name);
        }

        if(compare == 0) {
            compare = this.view.compareTo(other.view);
        }

        return compare;
    }
}
