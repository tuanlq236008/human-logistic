package com.demo.DataPreprocessor.process.cleaner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract comment counts from the dedicated comments field or from content.
 */
public final class CommentExtractor {

    private static final Pattern COMMENTS_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?K?)\\s*Comments", Pattern.CASE_INSENSITIVE);

    private CommentExtractor() {}

    public static Integer extract(String commentsCell, String content) {
        if (commentsCell != null) {
            Matcher m = COMMENTS_PATTERN.matcher(commentsCell);
            if (m.find()) {
                Integer v = NumberParser.parseNumberWithK(m.group(1));
                if (v != null) return v;
            }
        }
        if (content != null) {
            Matcher m = COMMENTS_PATTERN.matcher(content);
            if (m.find()) {
                Integer v = NumberParser.parseNumberWithK(m.group(1));
                if (v != null) return v;
            }
        }
        return null;
    }
}

