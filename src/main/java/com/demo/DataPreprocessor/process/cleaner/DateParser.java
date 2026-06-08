package com.demo.DataPreprocessor.process.cleaner;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse a short timestamp like "Nov 25" into yyyy-MM-dd using current year.
 */
public final class DateParser {

    private static final Pattern MONTH_DAY_PATTERN =
            Pattern.compile("(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d{1,2})",
                    Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter OUTPUT_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateParser() {}

    public static String parseDate(String timestamp) {
        if (timestamp == null) return null;

        String clean = TextUtils.removeIcons(timestamp);

        Matcher m = MONTH_DAY_PATTERN.matcher(clean);
        if (!m.find()) return null;

        String monRaw = m.group(1);
        int day = Integer.parseInt(m.group(2));

        int month;
        switch (monRaw.substring(0, 3).toLowerCase(Locale.ENGLISH)) {
            case "jan": month = 1; break;
            case "feb": month = 2; break;
            case "mar": month = 3; break;
            case "apr": month = 4; break;
            case "may": month = 5; break;
            case "jun": month = 6; break;
            case "jul": month = 7; break;
            case "aug": month = 8; break;
            case "sep": month = 9; break;
            case "oct": month = 10; break;
            case "nov": month = 11; break;
            case "dec": month = 12; break;
            default: return null;
        }

        int year = java.time.LocalDate.now().getYear();
        try {
            return java.time.LocalDate.of(year, month, day)
                    .format(OUTPUT_DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}

