package com.demo.DataPreprocessor.process.cleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Infer share count from numeric tokens in content, excluding reactions/comments.
 */
public final class ShareExtractor {

    private ShareExtractor() {}

    public static int extractShare(String content, int reactions, int comments) {
        if (content == null) return 0;

        Matcher m = NumberParser.tokenMatcher(content);
        List<String> tokens = new ArrayList<>();
        while (m.find()) tokens.add(m.group());

        int share = 0;
        for (int i = tokens.size() - 1; i >= 0; i--) {
            String token = tokens.get(i);
            Integer v = NumberParser.parseNumberWithK(token);
            if (v == null) continue;
            if (v == reactions || v == comments) continue;
            if (!token.toLowerCase().contains("k") && v > 1_000_000) continue;
            share = v; break;
        }
        return share;
    }
}

