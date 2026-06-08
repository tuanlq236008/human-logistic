package com.demo.UI.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;


public class SelectInputController extends BaseController {

    @FXML
    private javafx.scene.control.Label label;

    @FXML
    void btn1Pressed(ActionEvent event) {
        navigateTo(event, "/view/man_hinh_1.fxml", "Nhập thông tin cho bài toán", new Input1Controller());

    }

    @FXML
    void btn2Pressed(ActionEvent event) {
        navigateTo(event, "/view/tim_du_lieu_moi.fxml", "Nhập thông tin cho bài toán", new Input2Controller());
    }
    
    public void onGuiReady(Path csvPath) {
        System.err.println("man1Controller.onGuiReady called with: " + csvPath);
        try {
            if (csvPath != null && Files.exists(csvPath)) {
                System.err.println("CSV exists: " + csvPath.toAbsolutePath());
                // Inform the UI briefly
                if (label != null) {
                    label.setText("Loaded: " + csvPath.getFileName());
                }
                // Optionally: further loading of data can be placed here.
            } else {
                System.err.println("CSV not found at: " + (csvPath == null ? "(null)" : csvPath.toString()));
                if (label != null) label.setText("CSV not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // no-arg overload as a fallback
    public void onGuiReady() {
        System.err.println("man1Controller.onGuiReady() called (no path provided)");
        if (label != null) label.setText("Ready");
    }
}

    


