package com.demo.Utils.ConvertToCSV;

import java.io.FileInputStream;
import java.io.FileWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.opencsv.CSVWriter;

public class ExcelToCSV extends ToCSV {

    public ExcelToCSV(java.nio.file.Path filePath) {
        super(filePath);
    }

    @Override
    public void toCSV() {
        // Implementation for converting Excel to CSV goes here
        System.out.println("Converting Excel file at " + filePath + " to CSV at " + CSVPath);
         try (FileInputStream fis = new FileInputStream(filePath.toString());
            Workbook workbook = new XSSFWorkbook(fis);
            CSVWriter writer = new CSVWriter(new FileWriter(CSVPath.toString()))) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                StringBuilder sb = new StringBuilder();

                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            sb.append(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            sb.append(cell.getNumericCellValue());
                            break;
                        case BOOLEAN:
                            sb.append(cell.getBooleanCellValue());
                            break;
                        case FORMULA:
                            sb.append(cell.getCellFormula());
                            break;
                        default:
                            sb.append("");
                    }
                    sb.append(",");
                }

                // Xóa dấu ',' cuối dòng
                if (sb.length() > 0) sb.setLength(sb.length() - 1);

                writer.writeNext(sb.toString().split(","));
            }

            System.out.println("Chuyển thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }   
}
