package com.demo;
import com.demo.UI.controller.SelectInputController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String FXML_FILE_PATH = "/view/man_hinh_0.fxml";

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML_FILE_PATH));
        SelectInputController  man0Controller = new SelectInputController();
        fxmlLoader.setController(man0Controller);

        Parent root = fxmlLoader.load();

        // After the UI is loaded, try to locate the results CSV and notify the controller
        try {
            Path moduleRoot = resolveModuleRoot();
            Path sentimentCsv = null;
            if (moduleRoot != null) {
                Path candidate = moduleRoot.resolve("results/sentiment_timeline.csv");
                if (Files.exists(candidate)) sentimentCsv = candidate;
            }

            // Fallback to working directory
            if (sentimentCsv == null) {
                Path wdCandidate = Paths.get(System.getProperty("user.dir")).resolve("results/sentiment_timeline.csv");
                if (Files.exists(wdCandidate)) sentimentCsv = wdCandidate;
            }

            if (sentimentCsv != null) {
                com.demo.AppState.setSentimentCsvPath(sentimentCsv);
            }

            man0Controller.onGuiReady();
        } catch (Exception e) {
            System.err.println("GUI: could not resolve sentiment CSV: " + e.getMessage());
        }

        primaryStage.setTitle("nhập thông tin cho bài toán");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Path resolveModuleRoot() {
        try {
            File codeLoc = new File(GUI.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            Path cursor = codeLoc.toPath();
            for (int i = 0; i < 6 && cursor != null; i++) {
                if (Files.exists(cursor.resolve("pom.xml")) || Files.exists(cursor.resolve("src"))) {
                    return cursor.toAbsolutePath().normalize();
                }
                cursor = cursor.getParent();
            }
        } catch (Exception ignored) { }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

}