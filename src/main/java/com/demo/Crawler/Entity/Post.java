package com.demo.Crawler.Entity;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String platform;
    private String keyword;
    private String author;
    private String timePosted;
    private String content;
    private String reactionsCount;
    private String commentsCount;
    private List<String> imageUrls = new ArrayList<>();
    private List<String> comments = new ArrayList<>();

    public String getPlatform() {
        return platform;
    }
    public String getReactionsCount() {
        return reactionsCount;
        
    }
    public void setReactionsCount(String reactionsCount) {
        this.reactionsCount = reactionsCount;
        
    }
    public List<String> getImageUrls() {
        return imageUrls;
        
    }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        
    }
    public String getContent() {
        return content;
        
    }
    public void setContent(String content) {
        this.content = content;
        
    }
    public List<String> getComments() {
        return comments;
        
    }
    public void setComments(List<String> comments) {
        this.comments = comments;
        
    }
    public String getCommentsCount() {
        return commentsCount;
        
    }
    public void setCommentsCount(String commentsCount) {
        this.commentsCount = commentsCount;
        
    }
    public String getTimePosted() {
        return timePosted;
        
    }
    public void setTimePosted(String timePosted) {
        this.timePosted = timePosted;
        
    }
    public void setAuthor(String author) {
        this.author = author;
        
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
        
    }
    public void setPlatform(String platform) {
        this.platform = platform;
        
    }
    public String getKeyword() {
        return keyword;
    }
    public String getAuthor() {
        return author;
    }

    private String escapeCsv(String data) {
        if (data == null) return "\"\"";
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }

    public String toCsvRow() {
        String imagesAsString = String.join(";", this.getImageUrls());
        String commentsAsString = String.join(";", this.getComments());
        return String.join(",", escapeCsv(this.getPlatform()), escapeCsv(this.getKeyword()), escapeCsv(this.getAuthor()), escapeCsv(this.getTimePosted()), escapeCsv(this.getReactionsCount()), escapeCsv(this.getCommentsCount()), escapeCsv(this.getContent()), escapeCsv(imagesAsString), escapeCsv(commentsAsString));
    }
    
    public boolean isScrapeSuccessful() {
        return (this.getAuthor() != null && !this.getAuthor().isEmpty()) || (this.getContent() != null && !this.getContent().isEmpty());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- POST DETAILS ---\n");
        sb.append("Platform: ").append(getPlatform() != null ? getPlatform() : "N/A").append("\n");
        sb.append("Keyword: ").append(getKeyword() != null ? getKeyword() : "N/A").append("\n");
        sb.append("Author: ").append(getAuthor() != null ? getAuthor() : "Not found").append("\n");
        sb.append("Time: ").append(getTimePosted() != null ? getTimePosted() : "Not found").append("\n");
        sb.append("Reactions: ").append(getReactionsCount() != null ? getReactionsCount() : "Not found").append("\n");
        sb.append("Comments: ").append(getCommentsCount() != null ? getCommentsCount() : "Not found").append("\n");
        if (!getImageUrls().isEmpty()) {
            sb.append("Images (").append(getImageUrls().size()).append("):").append("\n");
            for (String url : getImageUrls()) { sb.append("  - ").append(url).append("\n"); }
        }
        sb.append("Content: ").append(getContent() != null ? "\n" + getContent() : "Not found").append("\n");
        if (!getComments().isEmpty()) {
            sb.append("Comments (").append(getComments().size()).append("):").append("\n");
            for (int i = 0; i < getComments().size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(getComments().get(i)).append("\n");
            }
        }
        sb.append("--------------------");
        return sb.toString();
    }
}