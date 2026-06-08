package com.demo.DataPreprocessor.io;

import com.demo.DataPreprocessor.model.CleanPost;

import java.io.IOException;
import java.util.List;

public interface PostWriter {
    void writePosts(String outputPath, List<CleanPost> posts) throws IOException;
}
