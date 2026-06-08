package com.demo.UI.controller;



import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import com.demo.UI.record.CsvLoader;
import com.demo.UI.record.SentimentData;

public class LineChartController extends BaseController implements Initializable {

    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawChartFromCSV();
    }
    @FXML
    void btnBackPressed(ActionEvent event) {
        navigateTo(event, "/view/chon_bai_toan.fxml", "Chọn bài toán mà bạn muốn giải quyết", new SelectExController());
    }

    @SuppressWarnings("unchecked")
    private void drawChartFromCSV() {
        // 1. Khởi tạo các Series
        XYChart.Series<String, Number> seriesPositive = new XYChart.Series<>();
        seriesPositive.setName("Tích cực");

        XYChart.Series<String, Number> seriesNegative = new XYChart.Series<>();
        seriesNegative.setName("Tiêu cực");

        XYChart.Series<String, Number> seriesNeutral = new XYChart.Series<>();
        seriesNeutral.setName("Trung lập");

        // 2. Gọi CsvLoader để lấy dữ liệu (Code xử lý file đã tách ra ngoài)
        CsvLoader loader = new CsvLoader();
        // Use relative path under project 'results' so CsvLoader can attempt to resolve it
        String csvFile = "results/sentiment_timeline.csv";
        List<SentimentData> dataList = loader.loadSentimentData(csvFile);

        if (dataList == null || dataList.isEmpty()) {
            System.err.println("LineChartController: Không có dữ liệu để hiển thị (tried '" + csvFile + "').");
            // Inform the user via Alert so it's visible in GUI
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Không có dữ liệu");
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy dữ liệu cảm xúc (sentiment_timeline.csv). Hãy chạy pipeline hoặc kiểm tra thư mục results.");
            alert.showAndWait();
            return;
        }

        // 3. Đổ dữ liệu vào các Series
        for (SentimentData item : dataList) {
            seriesPositive.getData().add(new XYChart.Data<>(item.getDate(), item.getPositive()));
            seriesNegative.getData().add(new XYChart.Data<>(item.getDate(), item.getNegative()));
            seriesNeutral.getData().add(new XYChart.Data<>(item.getDate(), item.getNeutral()));
        }

        // 4. Hiển thị lên biểu đồ
        if (lineChart != null) {
            lineChart.getData().clear();
            lineChart.getData().addAll(seriesPositive, seriesNegative, seriesNeutral);
        } else {
            System.err.println("LineChartController: lineChart is null (FXML not injected). Can't display chart.");
        }
    }
}