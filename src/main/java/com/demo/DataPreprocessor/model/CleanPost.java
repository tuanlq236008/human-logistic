package com.demo.DataPreprocessor.model;

public class CleanPost {
    private final int id;
    private final String author;
    private final String keyword;
    private final String date;   // yyyy-MM-dd
    private final int reactions;
    private final int comments;
    private final int share;
    private final String content;

    public CleanPost(int id,
                     String author,
                     String keyword,
                     String date,
                     int reactions,
                     int comments,
                     int share,
                     String content) {
        this.id = id;
        this.author = author;
        this.keyword = keyword;
        this.date = date;
        this.reactions = reactions;
        this.comments = comments;
        this.share = share;
        this.content = content;
    }

    public int getId() { return id; }
    public String getAuthor() { return author; }
    public String getKeyword() { return keyword; }
    public String getDate() { return date; }
    public int getReactions() { return reactions; }
    public int getComments() { return comments; }
    public int getShare() { return share; }
    public String getContent() { return content; }

    public String[] toCsvRow() {
        return new String[] {
                Integer.toString(id),
                author,
                keyword,
                date,
                Integer.toString(reactions),
                Integer.toString(comments),
                Integer.toString(share),
                content,
        };
    }
}


