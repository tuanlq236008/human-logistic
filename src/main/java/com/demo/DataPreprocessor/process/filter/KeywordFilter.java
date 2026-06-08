package com.demo.DataPreprocessor.process.filter;

import com.demo.DataPreprocessor.model.CleanPost;
import java.util.Locale;

public class KeywordFilter implements PostFilter {
    private final String keyword;

    public KeywordFilter(String keyword) {
        this.keyword = normalize(keyword);
    }

    public boolean accept(CleanPost post) {
        String k = normalize(post.getKeyword());
        return !keyword.isEmpty() && k.equals(keyword);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}

