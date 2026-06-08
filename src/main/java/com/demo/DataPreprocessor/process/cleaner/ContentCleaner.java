package com.demo.DataPreprocessor.process.cleaner;

/**
 * Clean the raw content string: remove icons, headers and trailing stats block.
 */
public final class ContentCleaner {

    private ContentCleaner() {}

    public static String cleanContent(String rawContent,
                                      String author,
                                      String timestamp,
                                      int reactions,
                                      int comments,
                                      int share) {
        if (rawContent == null) return "";

        String content = TextUtils.removeIcons(rawContent);

        String[] lines = content.split("\\R");
        int start = 0;
        while (start < lines.length && lines[start].trim().isEmpty()) start++;

        String authorNorm = TextUtils.normalizeForCompare(author);
        String tsNorm = TextUtils.normalizeForCompare(TextUtils.removeIcons(timestamp));

        int idx = start;
        StringBuilder acc = new StringBuilder();
        int lastMatchEnd = start;

        while (idx < lines.length && idx < start + 3) {
            if (acc.length() > 0) acc.append(' ');
            acc.append(lines[idx].trim());

            if (authorNorm.startsWith(TextUtils.normalizeForCompare(acc.toString()))) {
                lastMatchEnd = idx + 1;
                start = lastMatchEnd;
                idx++;
            } else break;
        }

        if (start < lines.length) {
            String lineNorm = TextUtils.normalizeForCompare(lines[start]);
            if (!tsNorm.isEmpty()
                    && (lineNorm.contains(tsNorm) || tsNorm.contains(lineNorm))) {
                start++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < lines.length; i++) {
            sb.append(lines[i]);
            if (i < lines.length - 1) sb.append('\n');
        }
        content = sb.toString().trim();

        return removeTrailingStatsBlock(content).trim();
    }

    private static String removeTrailingStatsBlock(String content) {
        String[] lines = content.split("\\R");
        int end = lines.length;

        java.util.regex.Pattern numLine =
                java.util.regex.Pattern.compile("^\\s*\\d+(?:\\.\\d+)?K?\\s*$",
                        java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Pattern numCommentsLine =
                java.util.regex.Pattern.compile("^\\s*\\d+(?:\\.\\d+)?K?\\s*Comments?\\s*$",
                        java.util.regex.Pattern.CASE_INSENSITIVE);

        while (end > 0) {
            String line = lines[end - 1].trim();
            if (line.isEmpty()
                    || numLine.matcher(line).matches()
                    || numCommentsLine.matcher(line).matches()) {
                end--;
            } else break;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < end; i++) {
            sb.append(lines[i]);
            if (i < end - 1) sb.append('\n');
        }
        return sb.toString();
    }
}

