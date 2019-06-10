package com.teamTK.tracker.model;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tracker {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("firstYear")
    @Expose
    private Integer firstYear;
    @SerializedName("firstMonth")
    @Expose
    private Integer firstMonth;

    @SerializedName("firstDay")
    @Expose
    private Integer firstDay;

    @SerializedName("legendColor")
    @Expose
    private List<LegendColor> legendColor = null;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;

    @SerializedName("active")
    @Expose
    private boolean active;

    @SerializedName("size")
    @Expose
    private int size;

    public Tracker() {

    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Tracker(String name, Integer firstYear, Integer firstMonth, Integer firstDay, List<LegendColor> legendColor, List<Datum> data, boolean active) {
        this.name = name;
        this.firstYear = firstYear;
        this.firstMonth = firstMonth;
        this.firstDay = firstDay;
        this.legendColor = legendColor;
        this.data = data;
        this.active = active;
        this.size = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFirstYear() {
        return firstYear;
    }

    public void setFirstYear(Integer firstYear) {
        this.firstYear = firstYear;
    }

    public Integer getFirstMonth() {
        return firstMonth;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setFirstMonth(Integer firstMonth) {
        this.firstMonth = firstMonth;
    }

    public Integer getFirstDay() {
        return firstDay;
    }

    public void setFirstDay(Integer firstDay) {
        this.firstDay = firstDay;
    }

    public List<LegendColor> getLegendColor() {
        return legendColor;
    }

    public void setLegendColor(List<LegendColor> legendColor) {
        this.legendColor = legendColor;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

}