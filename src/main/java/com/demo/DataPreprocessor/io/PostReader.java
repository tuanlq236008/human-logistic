package com.demo.DataPreprocessor.io;

import com.demo.DataPreprocessor.model.RawPost;

import java.io.IOException;
import java.util.List;

public interface PostReader {
    List<RawPost> readPosts(String inputPath) throws IOException;
}
