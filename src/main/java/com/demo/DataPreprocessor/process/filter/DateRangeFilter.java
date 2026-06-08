package com.demo.DataPreprocessor.process.filter;

import com.demo.DataPreprocessor.model.CleanPost;

import java.time.LocalDate;

public class DateRangeFilter implements PostFilter {

    private final LocalDate from;
    private final LocalDate to;

    public DateRangeFilter(String fromDate, String toDate) {
        this.from = (fromDate == null || fromDate.isBlank())
                ? null
                : LocalDate.parse(fromDate);

        this.to = (toDate == null || toDate.isBlank())
                ? null
                : LocalDate.parse(toDate);
    }

    public boolean accept(CleanPost post) {
        LocalDate d = LocalDate.parse(post.getDate());

        if (from != null && d.isBefore(from)) return false;
        if (to != null && d.isAfter(to)) return false;

        return true;
    }
}