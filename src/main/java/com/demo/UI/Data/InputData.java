package com.demo.UI.Data;

public class InputData {
    private String keyword;
    private String startDate;
    private String endDate;
    private String appSource;

    public InputData() {} 

    public InputData(String keyword, String startDate, String endDate, String appSource) {
        this.keyword = keyword;
        this.startDate = startDate;
        this.endDate = endDate;
        this.appSource = appSource;
    }

    // Getters
    public String getKeyword() { return keyword; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getAppSource() { return appSource; }
}
