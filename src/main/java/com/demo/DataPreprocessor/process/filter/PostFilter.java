package com.demo.DataPreprocessor.process.filter;

import com.demo.DataPreprocessor.model.CleanPost;

public interface PostFilter {
    boolean accept(CleanPost post);
}
