
package com.demo.UI.record;

public class SentimentRecord {
    private String sentiment;   //  positive, negative
    private int count;         
    private double percentage;  

    public SentimentRecord(String sentiment, int count, double percentage) {
        this.sentiment = sentiment;
        this.count = count;
        this.percentage = percentage;
    }

    public String getSentiment() { return sentiment; }
    public int getCount() { return count; }
    public double getPercentage() { return percentage; }

    public String getDisplayLabel() {
        return String.format("%s (%.1f%%)", sentiment, percentage);
    }
}
