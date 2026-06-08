package com.demo.UI.record;

public class DamageRecord {
    private String damageType; 
    private int count;         
    private double confidence; 
    private double percentage; // Lưu giá trị số (ví dụ: 31.9)
    private double total;
    private double economicLoss; 

    public DamageRecord(String damageType, int count, double confidence, double total, double economicLoss) {
        this.damageType = damageType;
        this.count = count;
        this.confidence = confidence;
        this.total = total; 
        this.economicLoss = economicLoss;
        
        // Tính toán ngay khi tạo đối tượng
        if (total > 0) {
            this.percentage = (count / total) * 100;
        } else {
            this.percentage = 0;
        }
    }

    // Getters
    public String getDamageType() { return damageType; }
    public int getCount() { return count; }
    public double getConfidence() { return confidence; }
    public double getTotal() { return total; }
    public double getEconomicLoss() { return economicLoss; }

    public double getPercentage() { 
        return percentage; 
    }
    
    public String getPercentageLabel() {
        return String.format("%.1f%%", percentage);
    }
}