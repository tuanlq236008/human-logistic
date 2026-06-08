package com.demo.DataPreprocessor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import com.demo.DataPreprocessor.io.CsvPostReader;
import com.demo.DataPreprocessor.io.CsvPostWriter;
import com.demo.DataPreprocessor.pipeline.ProcessingPipeline;
import com.demo.DataPreprocessor.process.filter.DateRangeFilter;
import com.demo.DataPreprocessor.process.filter.PostFilter;
import com.demo.DataPreprocessor.process.cleaner.PostCleaner;
import com.demo.DataPreprocessor.process.cleaner.FacebookPostCleaner;
import com.demo.DataPreprocessor.process.filter.KeywordFilter;
import com.demo.DataPreprocessor.process.filter.MergeFilter;
import com.demo.DataPreprocessor.config.AppConfig;
import com.demo.DataPreprocessor.config.ConfigLoader;

public class Main {

    public static void main(String[] args) {
        // --- BƯỚC 1: XÁC ĐỊNH FILE ĐẦU VÀO ---
        String input; // may be full path or just name
        String output;

        if (args.length >= 1 && args[0] != null && !args[0].isEmpty()) {
            // If caller passed an absolute path, use it directly
            input = args[0];
            // Ensure output is cleaned_<basename>
            Path p = Paths.get(input);
            String baseName = p.getFileName().toString();
            output = "cleaned_" + baseName;
            // If caller supplied explicit output name use it
            if (args.length >= 2 && args[1] != null && !args[1].isEmpty()) output = args[1];
        } else {
            // No args: attempt to find latest results_*.csv in module root
            File latestResultsFile = findLatestResultsFile();
            if (latestResultsFile == null) {
                System.err.println("Lỗi: Không tìm thấy file 'results_*.csv' nào tại thư mục gốc.");
                return;
            }
            input = latestResultsFile.getName();
            output = "cleaned_" + input;
        }

        // --- BƯỚC 2: LOAD FILE JSON TẠI THƯ MỤC GỐC ---
        System.out.println("Đang đọc cấu hình từ file: input_data.json ...");
        AppConfig cfg = ConfigLoader.load("input_data.json");

        // Kiểm tra nếu file JSON chưa được tạo hoặc lỗi
        if (cfg == null) {
            System.err.println("Không đọc được 'input_data.json' tại thư mục gốc.");
            return;
        }

        // Lấy dữ liệu từ config
        String from = cfg.startDate;
        String to = cfg.endDate;
        String keyword = cfg.keyword;

        System.out.println("============================================");
        System.out.println("Input File : " + input);
        System.out.println("Từ khoá    : " + keyword);
        System.out.println("Thời gian  : " + from + " -> " + to);
        System.out.println("============================================");

        // --- BƯỚC 3: CẤU HÌNH BỘ LỌC (FILTER) ---
        
        // 1. Lọc theo ngày
        PostFilter dateFilter = new DateRangeFilter(from, to);

        // 2. Lọc theo từ khóa (Nếu null hoặc rỗng thì bỏ qua lọc này)
        PostFilter keywordFilter = (keyword == null || keyword.isEmpty())
                ? post -> true // Giữ lại tất cả nếu không có keyword
                : new KeywordFilter(keyword);

        // 3. Gộp các bộ lọc lại
        PostFilter filter = new MergeFilter()
                .add(dateFilter)
                .add(keywordFilter);

        // --- BƯỚC 4: CẤU HÌNH LÀM SẠCH (CLEANER) ---
        PostCleaner cleaner = new FacebookPostCleaner();

        // --- BƯỚC 5: CHẠY PIPELINE ---
        ProcessingPipeline pipeline =
                new ProcessingPipeline(
                        new CsvPostReader(),
                        new CsvPostWriter(),
                        cleaner,
                        filter
                );

        try {
            System.out.println("Đang xử lý dữ liệu...");
            pipeline.run(input, output);
            System.out.println("Hoàn tất! File sạch đã được lưu tại: " + output);
        } catch (Exception e) {
            System.err.println("Đã xảy ra lỗi trong quá trình xử lý:");
            e.printStackTrace();
        }
    }

    private static File findLatestResultsFile() {
        // Try module root first (walk up from class location)
        try {
            File codeLoc = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            File cursor = codeLoc;
            for (int i = 0; i < 6 && cursor != null; i++) {
                File[] files = cursor.listFiles((dir, name) -> name.startsWith("results_") && name.endsWith(".csv"));
                if (files != null && files.length > 0) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                    return files[0];
                }
                cursor = cursor.getParentFile();
            }
        } catch (Exception ignored) { }

        // fallback to working directory
        File currentDir = new File(".");
        File[] files = currentDir.listFiles(
                (dir, name) -> name.startsWith("results_") && name.endsWith(".csv")
        );

        if (files == null || files.length == 0) {
            return null;
        }
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        return files[0];
    }
}