package com.demo.DataPreprocessor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class ConfigLoader {

    public static AppConfig load(String filePath) {
        try {
            //Đọc từ File System thay vì Classpath ---
            File file = new File(filePath);

            // Kiểm tra xem file có tồn tại không
            if (!file.exists()) {
                // In ra đường dẫn tuyệt đối nếu lỗi
                throw new RuntimeException("Không tìm thấy file cấu hình tại: " + file.getAbsolutePath());
            }

            ObjectMapper mapper = new ObjectMapper();
            
            // Đọc trực tiếp từ File
            AppConfig cfg = mapper.readValue(file, AppConfig.class);
            if (cfg.startDate != null && !cfg.startDate.isBlank()) {
                cfg.startDate = cfg.startDate.trim();
                try {
                    LocalDate.parse(cfg.startDate);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi định dạng Start Date (yyyy-MM-dd): " + cfg.startDate);
                }
            } else {
                cfg.startDate = null;
            }

            if (cfg.endDate != null && !cfg.endDate.isBlank()) {
                cfg.endDate = cfg.endDate.trim();
                try {
                    LocalDate.parse(cfg.endDate);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi định dạng End Date (yyyy-MM-dd): " + cfg.endDate);
                }
            } else {
                cfg.endDate = null;
            }

            if (cfg.keyword != null) {
                cfg.keyword = cfg.keyword.trim();
                if (cfg.keyword.isBlank()) cfg.keyword = null;
            }

            if (cfg.appSource != null) {
                cfg.appSource = cfg.appSource.trim();
            }

            return cfg;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file " + filePath + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý config: " + e.getMessage(), e);
        }
    }
}