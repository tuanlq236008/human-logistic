package com.demo.UI.record;


public class SentimentData {
    private String date;
    private int positive;
    private int negative;
    private int neutral;

    // Constructor
    public SentimentData(String date, int positive, int negative, int neutral) {
        this.date = date;
        this.positive = positive;
        this.negative = negative;
        this.neutral = neutral;
    }

    // Getter methods (để lấy dữ liệu ra dùng)
    public String getDate() { return date; }
    public int getPositive() { return positive; }
    public int getNegative() { return negative; }
    public int getNeutral() { return neutral; }
    
    @Override
    public String toString() {
        return "Date: " + date + " | Pos: " + positive + " | Neg: " + negative;
    }
}
