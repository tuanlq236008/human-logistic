package com.demo.UI.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;


public class SelectExController extends BaseController {
    @FXML
    void btnBackPressed(ActionEvent event) {
        navigateTo(event, "/view/man_hinh_1.fxml", "Nhập thông tin cho bài toán", new Input1Controller());
    }

    @FXML
    void btnEx1Pressed(ActionEvent event) {
        navigateTo(event, "/view/lineChart.fxml", "Bài 1", new LineChartController());
    }
    @FXML
    void btnEx2Pressed(ActionEvent event) {
        navigateTo(event, "/view/barChart.fxml", "Bài 2", new BarChartController());
    }
    
}
