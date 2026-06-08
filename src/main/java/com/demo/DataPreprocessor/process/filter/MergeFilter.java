package com.demo.DataPreprocessor.process.filter;

import com.demo.DataPreprocessor.model.CleanPost;
import java.util.ArrayList;
import java.util.List;

public class MergeFilter implements PostFilter {
    private final List<PostFilter> filters = new ArrayList<>();

    public MergeFilter add(PostFilter f) {
        if (f != null) filters.add(f);
        return this;
    }

    @Override
    public boolean accept(CleanPost post) {
        for (PostFilter f : filters) {
            if (!f.accept(post)) return false;
        }
        return true;
    }
}
