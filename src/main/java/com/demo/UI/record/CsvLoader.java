package com.demo.UI.record;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.demo.AppState;

public class CsvLoader {

    public List<SentimentData> loadSentimentData(String filePath) {
        List<SentimentData> dataList = new ArrayList<>();
        String line;
        String cvsSplitBy = ",";

        // First, if GUI has already resolved the CSV, prefer that
        Path appResolved = AppState.getSentimentCsvPath();
        if (appResolved != null && Files.exists(appResolved)) {
            filePath = appResolved.toAbsolutePath().toString();
        }

        String resolved = resolveCsvPath(filePath, "results/sentiment_timeline.csv", "sentiment_timeline.csv");
        System.err.println("CsvLoader: attempting to open sentiment CSV at: " + resolved);

        if (resolved == null || resolved.isEmpty() || !Files.exists(Paths.get(resolved))) {
            System.err.println("CsvLoader: CSV file not found. Tried: " + filePath + ", results/sentiment_timeline.csv, sentiment_timeline.csv");
            return dataList;
        }

        try (BufferedReader br = Files.newBufferedReader(Paths.get(resolved), StandardCharsets.UTF_8)) {
            // Bỏ qua dòng tiêu đề
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);

                // Kiểm tra dữ liệu hợp lệ
                if (data.length >= 4) {
                    try {
                        String date = data[0];
                        int positive = Integer.parseInt(data[1].trim());
                        int negative = Integer.parseInt(data[2].trim());
                        int neutral = Integer.parseInt(data[3].trim());

                        // Thêm vào danh sách
                        dataList.add(new SentimentData(date, positive, negative, neutral));
                    } catch (NumberFormatException e) {
                        System.err.println("Dòng dữ liệu lỗi format số: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file: " + e.getMessage());
            e.printStackTrace();
        }
        return dataList;
    }

    // Try to resolve a CSV path by checking the provided path, module root, and results/ subdirectory
    private String resolveCsvPath(String firstChoice, String... fallbacks) {
        // 1. If the provided path exists as-is, return it
        if (firstChoice != null) {
            try {
                Path p = Paths.get(firstChoice);
                if (Files.exists(p)) return p.toAbsolutePath().toString();
            } catch (Exception ignored) { }
        }

        // 2. Try resolving relative to module root (walk up from code location)
        try {
            File codeLoc = new File(CsvLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            Path cursor = codeLoc.toPath();
            for (int i = 0; i < 6 && cursor != null; i++) {
                for (String f : fallbacks) {
                    Path cand = cursor.resolve(f);
                    if (Files.exists(cand)) return cand.toAbsolutePath().toString();
                }
                cursor = cursor.getParent();
            }
        } catch (Exception ignored) { }

        // 3. Try working directory
        try {
            Path wd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
            for (String f : fallbacks) {
                Path cand = wd.resolve(f);
                if (Files.exists(cand)) return cand.toAbsolutePath().toString();
            }
        } catch (Exception ignored) { }

        // 4. Nothing found
        return null;
    }
}