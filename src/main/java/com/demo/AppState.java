package com.demo;

import java.nio.file.Path;

/**
 * Small in-memory holder for application-wide paths and flags used by the GUI.
 */
public class AppState {
    private static volatile Path sentimentCsvPath = null;

    public static Path getSentimentCsvPath() { return sentimentCsvPath; }
    public static void setSentimentCsvPath(Path p) { sentimentCsvPath = p; }
}

