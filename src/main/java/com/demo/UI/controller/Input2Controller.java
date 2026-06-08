package com.demo.UI.controller;

import com.demo.UI.Data.ConfigService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class Input2Controller extends BaseController implements Initializable {

    @FXML private ComboBox<String> cbAppSource; 
    @FXML private TextField keyword; // Nhập từ khóa tự do
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Tái sử dụng sourceList từ BaseController
        cbAppSource.setItems(sourceList);
        cbAppSource.getSelectionModel().selectFirst();
    }

    @FXML
    void btnContinuePressed(ActionEvent event) {
        // 1. Thu thập dữ liệu từ UI
        String keywordText = keyword.getText();
        String app = cbAppSource.getValue();
        String start = (dpStartDate.getValue() != null) ? dpStartDate.getValue().toString() : "";
        String end = (dpEndDate.getValue() != null) ? dpEndDate.getValue().toString() : "";

        // 2. Kiểm tra tính hợp lệ
        if (keywordText == null || keywordText.trim().isEmpty()) {
            showAlert("Thông báo", "Vui lòng nhập từ khóa tìm kiếm!", AlertType.WARNING);
            return;
        }

        // 3. Gọi Service để lưu dữ liệu (Tái sử dụng logic lưu file)
        boolean isSaved = ConfigService.saveAndUpdateConfig(keywordText, start, end, app);

        if (isSaved) {
            // 4. Chuyển màn hình nếu thành công
            changeScene(event);
        } else {
            showError("Lỗi hệ thống", "Không thể cập nhật cấu hình JSON.");
        }
    }

    @FXML
    void changeScene(ActionEvent event) {
        // Sử dụng hàm navigateTo từ BaseController của bạn
        navigateTo(event, "/view/progress2.fxml", "Đang xử lý dữ liệu",new Progress2Controller());
    }
}