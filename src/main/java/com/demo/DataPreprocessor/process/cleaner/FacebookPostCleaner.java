package com.demo.DataPreprocessor.process.cleaner;

import com.demo.DataPreprocessor.model.CleanPost;
import com.demo.DataPreprocessor.model.RawPost;

import java.util.Optional;

/**
 * PostCleaner chịu trách nhiệm chuyển đổi một RawPost
 * thành CleanPost bằng cách orchestrating các helper nhỏ hơn.
 */
public class FacebookPostCleaner implements PostCleaner {

    public Optional<CleanPost> clean(int id, RawPost raw) {

        // Chuẩn hoá các field đầu vào (tránh null, thừa khoảng trắng)
        String author = TextUtils.safeTrim(raw.getAuthor());
        String timestamp = TextUtils.safeTrim(raw.getTimestamp());
        String keyword = TextUtils.safeTrim(raw.getKeyword());
        String rawReactions = TextUtils.safeTrim(raw.getReactions());
        String rawComments = TextUtils.safeTrim(raw.getComments());
        String rawContent = raw.getContent() == null ? "" : raw.getContent();

        // Thiếu author hoặc timestamp → không phải bài post hợp lệ
        if (author.isEmpty() || timestamp.isEmpty()) {
            return Optional.empty();
        }

        // Parse và chuẩn hoá ngày đăng bài
        String date = DateParser.parseDate(timestamp);
        if (date == null) {
            return Optional.empty();
        }

        // Parse số reaction
        Integer reactions = NumberParser.parseNumberWithK(rawReactions);
        if (reactions == null) {
            return Optional.empty();
        }

        // Parse số comment (từ cột comments hoặc trong content)
        Integer comments = CommentExtractor.extract(rawComments, rawContent);
        if (comments == null) comments = 0;

        // Suy ra số share từ block số cuối của content
        int share = ShareExtractor.extractShare(rawContent, reactions, comments);

        // Làm sạch content với lớp chuyên trách
        String cleanContent = ContentCleaner.cleanContent(
                rawContent, author, timestamp, reactions, comments, share
        );

        if (cleanContent.isEmpty()) return Optional.empty();

        CleanPost cleanPost = new CleanPost(
                id,
                author,
                keyword,
                date,
                reactions,
                comments,
                share,
                cleanContent
        );

        return Optional.of(cleanPost);
    }
}
