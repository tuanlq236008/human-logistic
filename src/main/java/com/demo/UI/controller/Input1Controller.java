package com.demo.UI.controller;

import javafx.collections.FXCollections;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;
import com.demo.UI.Data.ConfigService;

public class Input1Controller extends BaseController implements Initializable {
    @FXML private ComboBox<String> cbAppSource, cbKeyWord;
    @FXML private DatePicker dpStartDate, dpEndDate;
    @FXML private Label label;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbAppSource.setItems(sourceList);
        cbAppSource.getSelectionModel().selectFirst();
        cbKeyWord.setItems(FXCollections.observableArrayList("matmo", "bão wipha", "bão số 8", "bão noru",
            "bão koinu", "lũ lụt miền trung", "thiên tai việt nam", "bão yagi"
    ));
    }

    @FXML
    void btnContinuePressed(ActionEvent event) {
        handleSaveAndMove(event, cbKeyWord.getValue());
    }

    private void handleSaveAndMove(ActionEvent event, String kw) {
        String start = (dpStartDate.getValue() != null) ? dpStartDate.getValue().toString() : "";
        String end = (dpEndDate.getValue() != null) ? dpEndDate.getValue().toString() : "";

        if (kw == null || kw.isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng chọn từ khóa!", Alert.AlertType.WARNING);
            return;
        }

        if (ConfigService.saveAndUpdateConfig(kw, start, end, cbAppSource.getValue())) {
            navigateTo(event, "/view/progress.fxml", "Đang xử lý", new ProgressController());
        } else {
            showError("Lỗi", "Không thể lưu dữ liệu cấu hình!");
        }
    }


}
    