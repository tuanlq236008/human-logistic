package com.demo.DataPreprocessor.io;

import com.demo.DataPreprocessor.model.RawPost;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvPostReader implements PostReader {

    @Override
    public List<RawPost> readPosts(String inputPath) throws IOException {
        List<RawPost> posts = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(Paths.get(inputPath), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                String platform = record.get("Platform");
                String keyword = record.get("Keyword");
                String author = record.get("Author");
                String timestamp = record.get("Timestamp");
                String reactions = record.get("Reactions");
                String comments = record.get("Comments");
                String content = record.get("Content");
                String imageUrl = record.isMapped("Image URLs") ? record.get("Image URLs") : "";

                RawPost post = new RawPost(
                        platform,
                        keyword,
                        author,
                        timestamp,
                        reactions,
                        comments,
                        content,
                        imageUrl
                );
                posts.add(post);
            }
        }

        return posts;
    }
}
