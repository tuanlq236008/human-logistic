package com.demo.Utils.ConvertToCSV;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ToCSV {
    protected Path filePath;;
    protected Path CSVPath;
    protected String extension;
    
    public ToCSV(Path filePath) {
        this.filePath = filePath;
        this.CSVPath = filePath.resolveSibling(
            filePath.getFileName().toString().replaceFirst("[.][^.]+$", "") + ".csv"
        );
    }

    public boolean isValid(){
        return Files.exists(filePath);
    }

    public String getExtension(){
        if (extension == null) {
            String fileName = filePath.getFileName().toString();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                extension = fileName.substring(dotIndex + 1).toLowerCase();
            } else {
                extension = "";
            }
        }
        return extension;
    }

    public abstract void toCSV();

    public Path getCSVPath() {
        return CSVPath;
    }

}