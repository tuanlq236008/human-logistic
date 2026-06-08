package com.demo.DataPreprocessor.io;

import com.demo.DataPreprocessor.model.CleanPost;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CsvPostWriter implements PostWriter {

    @Override
    public void writePosts(String outputPath, List<CleanPost> posts) throws IOException {
        try (BufferedWriter writer =
                     Files.newBufferedWriter(Paths.get(outputPath), StandardCharsets.UTF_8)) {

            writer.write("id,authors,keyword,Date,Reactions,COMMENT,Share,content");
            writer.newLine();

            for (CleanPost post : posts) {
                String[] cols = post.toCsvRow();

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < cols.length; i++) {
                    String col = cols[i] == null ? "" : cols[i];
                    boolean needQuote = col.contains(",") || col.contains("\n") || col.contains("\"");
                    if (needQuote) {
                        col = col.replace("\"", "\"\"");
                        sb.append('"').append(col).append('"');
                    } else {
                        sb.append(col);
                    }
                    if (i < cols.length - 1) sb.append(',');
                }
                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }
}
