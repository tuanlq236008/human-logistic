package com.demo.DataPreprocessor.process.cleaner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse numbers, including values like 3.5K -> 3500
 */
public final class NumberParser {

    private static final Pattern NUMBER_OR_K =
            Pattern.compile("(\\d+(?:\\.\\d+)?)K", Pattern.CASE_INSENSITIVE);

    private static final Pattern PLAIN_NUMBER = Pattern.compile("\\d+");

    private static final Pattern NUMBER_TOKEN_PATTERN =
            Pattern.compile("\\d+(?:\\.\\d+)?K?");

    private NumberParser() {}

    public static Integer parseNumberWithK(String text) {
        if (text == null) return null;
        text = text.trim();
        if (text.isEmpty()) return null;

        Matcher mK = NUMBER_OR_K.matcher(text);
        if (mK.matches()) {
            double base = Double.parseDouble(mK.group(1));
            return (int) Math.round(base * 1000);
        }

        Matcher mPlain = PLAIN_NUMBER.matcher(text);
        if (mPlain.matches()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public static Matcher tokenMatcher(CharSequence input) {
        return NUMBER_TOKEN_PATTERN.matcher(input);
    }
}

