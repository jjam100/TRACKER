package com.teamTK.tracker.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LegendColor {

    @SerializedName("legendName")
    @Expose
    private String legendName;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("active")
    @Expose
    private boolean active;


    public LegendColor() {}

    public LegendColor(String legendName, String color, boolean active) {
        this.legendName = legendName;
        this.color = color;
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLegendName() {
        return legendName;
    }

    public void setLegendName(String legendName) {
        this.legendName = legendName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}