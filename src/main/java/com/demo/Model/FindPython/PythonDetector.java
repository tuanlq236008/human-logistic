package com.demo.Model.FindPython;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PythonDetector {

    public static String findPythonExecutable() {
        // Ưu tiên venv của project (ĐÚNG OS)
        String venvPython = getProjectVenvPython();
        if (venvPython != null) {
            return venvPython;
        }

        // Windows: tìm python native mới nhất
        if (isWindows()) {
            return findBestWindowsPython();
        }

        // macOS / Linux
        return findBestUnixPython();
    }

    // ===================== HELPERS =====================

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    // ---------- 1. Project venv ----------
    private static String getProjectVenvPython() {
        Path venv = Paths.get(".venv");
        if (!Files.exists(venv)) return null;

        Path python = isWindows()
                ? venv.resolve("Scripts/python.exe")
                : venv.resolve("bin/python");

        if (Files.exists(python)) {
            return python.toAbsolutePath().toString();
        }
        return null;
    }

    // ---------- 2. Windows ----------
    private static String findBestWindowsPython() {
        List<String> candidates = new ArrayList<>();

        try {
            Process p = new ProcessBuilder("where", "python").start();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream())
            );

            String line;
            while ((line = br.readLine()) != null) {
                String lower = line.toLowerCase();

                // loại python rác
                if (lower.contains("msys")
                        || lower.contains("mingw")
                        || lower.contains("ucrt")
                        || lower.contains("windowsapps")) {
                    continue;
                }

                candidates.add(line.trim());
            }
        } catch (IOException ignored) {}

        return pickNewestPython(candidates, "python");
    }

    // ---------- 3. macOS / Linux ----------
    private static String findBestUnixPython() {
        List<String> candidates = List.of("python3", "python");
        return pickNewestPython(candidates, null);
    }

    // ---------- Version picker ----------
    private static String pickNewestPython(List<String> candidates, String fallback) {
        String bestPython = null;
        Version bestVersion = null;

        for (String cmd : candidates) {
            Version v = getPythonVersion(cmd);
            if (v == null) continue;

            if (bestVersion == null || v.compareTo(bestVersion) > 0) {
                bestVersion = v;
                bestPython = cmd;
            }
        }
        return bestPython != null ? bestPython : fallback;
    }

    private static Version getPythonVersion(String pythonCmd) {
        try {
            Process p = new ProcessBuilder(pythonCmd, "--version").start();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            p.getInputStream().available() > 0
                                    ? p.getInputStream()
                                    : p.getErrorStream()
                    )
            );

            String line = br.readLine(); // Python 3.13.0
            if (line == null || !line.startsWith("Python")) return null;

            String[] parts = line.replace("Python ", "").split("\\.");
            return new Version(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            );
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- Version record ----------
    private record Version(int major, int minor, int patch)
            implements Comparable<Version> {
        @Override
        public int compareTo(Version o) {
            if (major != o.major) return major - o.major;
            if (minor != o.minor) return minor - o.minor;
            return patch - o.patch;
        }
    }
}

