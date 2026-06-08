package com.demo.UI.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public abstract class BaseController {

    // Danh sách App dùng chung cho cả 2 màn hình
    protected final javafx.collections.ObservableList<String> sourceList = 
        javafx.collections.FXCollections.observableArrayList("FaceBook", "Youtube", "TikTok", "Instagram");

    // Hàm hiện thông báo chung
    protected void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void navigateTo(ActionEvent event, String fxmlPath, String title, Object controller) {
        URL fxmlLocation = getClass().getResource(fxmlPath);
        if (fxmlLocation == null) {
            showError("Lỗi hệ thống", "Không tìm thấy file giao diện tại: " + fxmlPath);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            if (controller != null) {
                loader.setController(controller);
            }
            Parent newNode = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene currentScene = stage.getScene();
            if (currentScene != null) {
                currentScene.setRoot(newNode);
            } else {
                stage.setScene(new Scene(newNode));
            }
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Lỗi tải giao diện", "Không thể hiển thị màn hình: " + e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    protected void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Lỗi ứng dụng");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}