package com.teamTK.tracker.model;

public class Tracker {
    private String name;
    private Data[] datas;
    private LegendColor[] legendColors;
    private int firstYear;
    private int firstMonth;
    private int firstDay;

    public void setName(String name) { this.name = name; }

    public void setDatas(Data[] datas) { this.datas = datas; }

    public void setLegendColors(LegendColor[] legendColors) { this.legendColors = legendColors; }

    public void setFirstYear(int firstYear) { this.firstYear = firstYear; }

    public void setFirstMonth(int firstMonth) { this.firstMonth = firstMonth; }

    public void setFirstDay(int firstDay) { this.firstDay = firstDay; }

    public String getName() { return name; }

    public Data[] getDatas() { return datas; }

    public LegendColor[] getLegendColors() { return legendColors; }

    public int getFirstYear() { return firstYear; }

    public int getFirstMonth() { return firstMonth; }

    public int getFirstDay() { return firstDay; }



}
