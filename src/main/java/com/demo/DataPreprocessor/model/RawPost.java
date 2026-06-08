package com.demo.DataPreprocessor.model;

public class RawPost {
    private final String platform;
    private final String keyword;
    private final String author;
    private final String timestamp;
    private final String reactions;
    private final String comments;
    private final String content;
    private final String imageUrl;


    public RawPost(String platform,
                   String keyword,
                   String author,
                   String timestamp,
                   String reactions,
                   String comments,
                   String content,
                   String imageUrl
                   ) {
        this.platform = platform;
        this.keyword = keyword;
        this.author = author;
        this.timestamp = timestamp;
        this.reactions = reactions;
        this.comments = comments;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public String getPlatform() { return platform; }
    public String getKeyword() { return keyword; }
    public String getAuthor() { return author; }
    public String getTimestamp() { return timestamp; }
    public String getReactions() { return reactions; }
    public String getComments() { return comments; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
}
