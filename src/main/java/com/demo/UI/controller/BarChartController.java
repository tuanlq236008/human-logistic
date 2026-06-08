package com.demo.UI.controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.demo.UI.record.DamageRecord;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis; 
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BarChartController extends BaseController {

    @FXML
    private BarChart<String, Number> damageBarChart;

    @FXML
    public void initialize() {
        System.out.println("Giao diện đã khởi tạo, đang load dữ liệu...");
        updateDamageChart();
    }
    @FXML
    void btnBackPressed(ActionEvent event) {
        navigateTo(event, "/view/chon_bai_toan.fxml", "Chọn bài toán mà bạn muốn giải quyết", new SelectExController());
    }

    public void updateDamageChart() {
        String csvPath = "results/damage_statistics.csv";

        if (!Files.exists(Paths.get(csvPath))) {
            System.err.println("BarChartController: file not found: " + csvPath);
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Không có dữ liệu");
            a.setHeaderText(null);
            a.setContentText("Không tìm thấy file damage_statistics.csv trong thư mục results. Hãy chạy pipeline trước khi mở biểu đồ.");
            a.showAndWait();
            return;
        }

        List<DamageRecord> records = loadCsvData(csvPath);

        if (records.isEmpty()) {
            System.err.println(" Không đọc được dữ liệu nào từ file: " + csvPath);
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Dữ liệu trống");
            a.setHeaderText(null);
            a.setContentText("File damage_statistics.csv rỗng hoặc không hợp lệ.");
            a.showAndWait();
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tỷ lệ thiệt hại (%)");

        for (DamageRecord record : records) {
            series.getData().add(new XYChart.Data<>(record.getDamageType(), record.getPercentage()));
        }

        if (damageBarChart != null) {
            damageBarChart.getData().clear();
            damageBarChart.getData().add(series);
            if (damageBarChart.getYAxis() instanceof NumberAxis) {
                ((NumberAxis) damageBarChart.getYAxis()).setLabel("Tỷ lệ phần trăm (%)");
            }
        } else {
            System.err.println("BarChartController: damageBarChart is null (FXML not injected). Can't display chart.");
        }
    }

    public List<DamageRecord> loadCsvData(String filePath) {
        List<DamageRecord> dataList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isHeader = true;
            
            while ((line = br.readLine()) != null) {
                // Bỏ qua dòng tiêu đề đầu tiên
                if (isHeader) { isHeader = false; continue; }
                
                // Tách chuỗi CSV
                String[] values = line.split(",");
                
                // File summary có 4 cột: total_posts, damage_type, post_count, confidence
                if (values.length >= 4) {
                    try {
                        double total = Double.parseDouble(values[0].trim());
                        String type = values[1].trim();
                        int count = Integer.parseInt(values[2].trim());
                        double conf = Double.parseDouble(values[3].trim());

                        // economicLoss may be missing; default to 0 if not provided
                        double economicLoss = 0.0;
                        if (values.length >= 5) {
                            try { economicLoss = Double.parseDouble(values[4].trim()); } catch (NumberFormatException ignored) { }
                        }

                        dataList.add(new DamageRecord(type, count, conf, total, economicLoss));
                        
                    } catch (NumberFormatException nfe) {
                        System.err.println("Lỗi định dạng số ở dòng: " + line);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc file CSV: " + e.getMessage());
            e.printStackTrace();
        }
        return dataList;
    }
}