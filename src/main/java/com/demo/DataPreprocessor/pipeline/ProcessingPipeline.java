package com.demo.DataPreprocessor.pipeline;

import com.demo.DataPreprocessor.io.PostReader;
import com.demo.DataPreprocessor.io.PostWriter;
import com.demo.DataPreprocessor.model.CleanPost;
import com.demo.DataPreprocessor.model.RawPost;
import com.demo.DataPreprocessor.process.cleaner.PostCleaner;
import com.demo.DataPreprocessor.process.filter.PostFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ProcessingPipeline {

    private final PostReader reader;
    private final PostWriter writer;
    private final PostCleaner cleaner;
    private final PostFilter filter;

    public ProcessingPipeline(PostReader reader,
                              PostWriter writer,
                              PostCleaner cleaner,
                              PostFilter filter) {
        this.reader = reader;
        this.writer = writer;
        this.cleaner = cleaner;
        this.filter = filter;
    }

    public ProcessingPipeline(PostReader reader,
                              PostWriter writer,
                              PostCleaner cleaner) {
        this(reader, writer, cleaner, null);
    }

    public void run(String inputPath, String outputPath) throws IOException {
        List<RawPost> rawPosts = reader.readPosts(inputPath);
        List<CleanPost> cleanPosts = new ArrayList<>();

        int id = 1;
        for (RawPost raw : rawPosts) {

            // Pipeline chỉ gọi cleaner qua interface
            Optional<CleanPost> opt = cleaner.clean(id, raw);
            if (opt.isEmpty()) continue;

            CleanPost cp = opt.get();

            if (filter != null) {
                boolean accepted;
                try {
                    accepted = filter.accept(cp);
                } catch (Exception e) {
                    accepted = false;
                }
                if (!accepted) continue;
            }

            cleanPosts.add(cp);
            id++;
        }

        writer.writePosts(outputPath, cleanPosts);
    }
}
