package com.demo.DataPreprocessor.process.cleaner;

import com.demo.DataPreprocessor.model.CleanPost;
import com.demo.DataPreprocessor.model.RawPost;

import java.util.Optional;

public interface PostCleaner {
    Optional<CleanPost> clean(int id, RawPost raw);
}
