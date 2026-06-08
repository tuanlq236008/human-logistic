package com.demo.Utils.ConvertToCSV;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SQLToCSV extends ToCSV {

    public SQLToCSV(Path filePath) {
        super(filePath);
    }

    @Override
    public void toCSV() {
        try {
            String sql = Files.readString(filePath, StandardCharsets.UTF_8);
            List<String> header = null;
            List<String[]> rows = new ArrayList<>();

            // Normalize whitespace for easier matching
            String lower = sql.toLowerCase();
            int idx = 0;
            while (true) {
                int ins = lower.indexOf("insert", idx);
                if (ins == -1) break;

                // find opening parenthesis for columns list after table name
                int colsStart = lower.indexOf('(', ins);
                if (colsStart == -1) { idx = ins + 6; continue; }
                int colsEnd = findClosingParen(lower, colsStart);
                if (colsEnd == -1) { idx = ins + 6; continue; }

                String colsText = sql.substring(colsStart + 1, colsEnd).trim();
                List<String> cols = splitRespectingQuotes(colsText);
                if (header == null) header = new ArrayList<>(cols.size());
                for (int i = 0; i < cols.size(); i++) {
                    header.add(cols.get(i).replaceAll("^\\s*`|`\\s*$", "").replaceAll("^\\s*\"|\"\\s*$", "").trim());
                }

                // find VALUES keyword after colsEnd
                int valuesIdx = lower.indexOf("values", colsEnd);
                if (valuesIdx == -1) { idx = colsEnd + 1; continue; }

                // extract tuples after VALUES
                int pos = valuesIdx + 6;
                while (pos < sql.length()) {
                    // skip whitespace and commas
                    char c = sql.charAt(pos);
                    if (Character.isWhitespace(c) || c == ',') { pos++; continue; }
                    if (c != '(') break;

                    int end = findClosingParen(sql, pos);
                    if (end == -1) break;

                    String tuple = sql.substring(pos + 1, end);
                    List<String> values = splitRespectingQuotes(tuple);
                    String[] row = new String[values.size()];
                    for (int i = 0; i < values.size(); i++) {
                        row[i] = normalizeValue(values.get(i));
                    }
                    rows.add(row);
                    pos = end + 1;
                }

                idx = colsEnd + 1;
            }

            if (header == null) {
                // No INSERTs found — try to treat each line as a row of values separated by whitespace/comma
                List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    List<String> parts = splitRespectingQuotes(line);
                    rows.add(parts.toArray(new String[0]));
                }
            }

            // Ensure CSV parent exists
            Path parent = CSVPath.getParent();
            if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);

            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(Files.newOutputStream(CSVPath), StandardCharsets.UTF_8))) {
                if (header != null && !header.isEmpty()) {
                    writer.writeNext(header.toArray(new String[0]));
                }
                for (String[] r : rows) writer.writeNext(r);
            }

            System.out.println("Wrote CSV to " + CSVPath + " (rows=" + rows.size() + ")");

        } catch (IOException e) {
            System.err.println("Failed to convert SQL to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int findClosingParen(String s, int openPos) {
        int depth = 0;
        boolean inQuote = false;
        for (int i = openPos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                inQuote = !inQuote; // toggle on single quote
            }
            if (inQuote) continue;
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static List<String> splitRespectingQuotes(String s) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                inQuote = !inQuote;
                cur.append(c);
            } else if (c == ',' && !inQuote) {
                parts.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) parts.add(cur.toString().trim());
        return parts;
    }

    private static String normalizeValue(String raw) {
        String v = raw.trim();
        if (v.equalsIgnoreCase("null")) return "";
        if (v.startsWith("'") && v.endsWith("'")) {
            // remove surrounding single quotes and unescape doubled single quotes
            String inner = v.substring(1, v.length() - 1).replace("''", "'");
            return inner;
        }
        if (v.startsWith("\"") && v.endsWith("\"")) {
            return v.substring(1, v.length() - 1).replace("\"\"", "\"");
        }
        return v;
    }

}