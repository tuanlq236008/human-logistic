package com.demo.UI.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;


import javafx.concurrent.Task;
import javafx.scene.control.Label;

public class Progress2Controller extends BaseController {
    @FXML
    private Label label;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ProgressIndicator progressIndicator;

    doWork task;

    @FXML
    void cancel(ActionEvent event) {
        task.cancel();
        progressBar.progressProperty().unbind();
        progressIndicator.progressProperty().unbind();
        progressBar.setProgress(0);
        progressIndicator.setProgress(0);
        label.textProperty().unbind();
        label.setText("Cancelled");
    }


    @FXML
    void start(ActionEvent event) {
        task = new doWork();
        progressBar.progressProperty().bind(task.progressProperty());
        progressIndicator.progressProperty().bind(task.progressProperty());
        label.textProperty().bind(task.messageProperty());
        new Thread(task).start();

    }
    @FXML
    void btnContinuePressed(ActionEvent event) {
        navigateTo(event, "/view/chon_bai_toan.fxml", "Chọn bài toán mà bạn muốn giải quyết", new SelectExController());

    }

}
class doWork extends Task<Void> {
    @Override
    protected Void call() throws Exception {
        updateMessage("Đang chuẩn bị Pipeline...");
        updateProgress(0.1, 1.0); // Bắt đầu 10%

        // Gọi logic từ App.java
        try {
            updateMessage("Data Preprocessor...");
            
            com.demo.App.runFullPipeline(); 
            
            updateProgress(1.0, 1.0);
            updateMessage("Hoàn thành thành công!");
        } catch (Exception e) {
            updateMessage("Lỗi: " + e.getMessage());
            throw e; 
        }
        
        return null;
    }
}
