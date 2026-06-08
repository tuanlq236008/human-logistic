package com.demo.DataPreprocessor.process.cleaner;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Small text utilities extracted from FacebookPostCleaner to follow SRP.
 */
public final class TextUtils {

    private TextUtils() { }

    public static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    public static String removeIcons(String s) {
        if (s == null) return "";
        return s.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}\\n]", "");
    }

    public static String normalizeForCompare(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return n.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}

