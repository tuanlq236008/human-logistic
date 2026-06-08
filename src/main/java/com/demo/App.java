package com.demo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import static com.demo.Model.FindPython.PythonDetector.findPythonExecutable;

public class App {
    private static final Scanner scanner = new Scanner(System.in);
    
    // ĐỊNH NGHĨA TÊN FILE CỐ ĐỊNH Ở ĐÂY
    private static final String INPUT_FILE_NAME = "results_2025-12-13_11-45-27.csv"; 
    private static final String DEBUG_LOG = "pipeline_debug.log";

    public static void main(String[] args) {
        try {
            showMenu();
        } finally {
            scanner.close();
        }
    }

    private static void showMenu() {
        while (true) {
            System.out.println("\n=== Project Pipeline Runner (Auto-mode) ===");
            System.out.println("1. Run Pipeline using: " + INPUT_FILE_NAME);
            System.out.println("2. Exit");
            System.out.print("Choose: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); 

                if (choice == 1) {
                    runPipelineFromFixedFile(); 
                } else {
                    System.out.println("Exiting...");
                    return;
                }
            } else {
                scanner.nextLine();
            }
        }
    }

    public static void runPipelineFromFixedFile() {
        try {
            Path moduleRoot = getModuleRoot();
            debugLog("Determined module root: " + moduleRoot.toAbsolutePath());
            debugLog("Working dir (user.dir): " + System.getProperty("user.dir"));

            // 1. Kiểm tra file có tồn tại không (resolve relative to module root)
            File selectedFile = moduleRoot.resolve(INPUT_FILE_NAME).toFile();
            debugLog("Expected input file (moduleRoot resolved): " + selectedFile.getAbsolutePath());

            if (!selectedFile.exists()) {
                System.err.println("ERROR: File '" + INPUT_FILE_NAME + "' not found in: " + selectedFile.getAbsolutePath());
                debugLog("ERROR: Input file not found: " + selectedFile.getAbsolutePath());
                return;
            }

            System.out.println("\n[0] Target file found: " + selectedFile.getAbsolutePath());
            debugLog("Target file found: " + selectedFile.getAbsolutePath());

            // 2. Chạy bộ tiền xử lý (Data Processor)
            System.out.println("\n[1] Running Data Processor...");
            com.demo.DataPreprocessor.Main.main(new String[]{
                    selectedFile.getAbsolutePath()
            });

            // Tìm file cleaned_... trong module root
            File processedOutput = findLatestFileInDir(moduleRoot, "cleaned_", ".csv");
            if (processedOutput == null) {
                throw new RuntimeException("Processor did not produce cleaned_*.csv");
            }
            System.out.println("Processed output: " + processedOutput.getAbsolutePath());
            debugLog("Processed output: " + processedOutput.getAbsolutePath());

            // 3. Chạy Mô hình Python
            System.out.println("\n[2] Running Python Model...");
            runPythonModel(processedOutput.getAbsolutePath(), moduleRoot);

            System.out.println("\n=== PIPELINE FINISHED SUCCESSFULLY ===");
            debugLog("PIPELINE FINISHED SUCCESSFULLY");

        } catch (Exception e) {
            System.err.println("\n❌ PIPELINE FAILED");
            e.printStackTrace();
            debugLog("PIPELINE FAILED: " + e.toString());
        }
    }

    /**
     * Run the full end-to-end flow: Crawler -> DataPreprocessor -> Python Model
     * This is intended for the GUI "start2" path where no fixed input file exists yet.
     */
    public static void runFullPipeline() {
        try {
            Path moduleRoot = getModuleRoot();
            debugLog("Determined module root: " + moduleRoot.toAbsolutePath());
            debugLog("Working dir (user.dir): " + System.getProperty("user.dir"));

            // 0. Run the crawler (uses com.demo.Crawler.Main)
            System.out.println("\n[0] Running Crawler (this may open a browser)...");
            debugLog("Starting crawler main");
            // Pass the module-level config.json so the crawler reads the intended config
            com.demo.Crawler.Main.main(new String[]{ moduleRoot.resolve("config.json").toString() });

            // 1. After crawler finishes, locate the latest results_*.csv produced
            File resultsFile = findLatestFileInDir(moduleRoot, "results_", ".csv");
            if (resultsFile == null) {
                throw new RuntimeException("Crawler did not produce results_*.csv");
            }
            System.out.println("Found crawler output: " + resultsFile.getAbsolutePath());
            debugLog("Found crawler output: " + resultsFile.getAbsolutePath());

            // 2. Run Data Preprocessor on the crawler output
            System.out.println("\n[1] Running Data Processor on crawler output...");
            com.demo.DataPreprocessor.Main.main(new String[]{ resultsFile.getAbsolutePath() });

            // 3. Find cleaned CSV
            File processedOutput = findLatestFileInDir(moduleRoot, "cleaned_", ".csv");
            if (processedOutput == null) {
                throw new RuntimeException("Processor did not produce cleaned_*.csv");
            }
            System.out.println("Processed output: " + processedOutput.getAbsolutePath());
            debugLog("Processed output: " + processedOutput.getAbsolutePath());

            // 4. Run Python model on processed output
            System.out.println("\n[2] Running Python Model...");
            runPythonModel(processedOutput.getAbsolutePath(), moduleRoot);

            System.out.println("\n=== FULL PIPELINE FINISHED SUCCESSFULLY ===");
            debugLog("FULL PIPELINE FINISHED SUCCESSFULLY");

        } catch (Exception e) {
            System.err.println("\n❌ FULL PIPELINE FAILED");
            e.printStackTrace();
            debugLog("FULL PIPELINE FAILED: " + e.toString());
        }
    }



    // ================= FILE UTILS =================

    

    private static File findLatestFileInDir(Path dir, String prefix, String suffix) {
        File[] files = dir.toFile().listFiles((d, name) -> name.startsWith(prefix) && name.endsWith(suffix));

        if (files == null || files.length == 0) return null;

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        return files[0];
    }

    private static Path getModuleRoot() {
        // Try code location first and walk up until we find pom.xml or src dir
        try {
            File codeLoc = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            Path cursor = codeLoc.toPath();
            for (int i = 0; i < 6 && cursor != null; i++) {
                if (Files.exists(cursor.resolve("pom.xml")) || Files.exists(cursor.resolve("src"))) {
                    return cursor.toAbsolutePath().normalize();
                }
                cursor = cursor.getParent();
            }
        } catch (Exception ignored) { }

        // Fallback to user.dir
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    // ================= PYTHON RUNNER (ĐÃ SỬA PATH) =================

    private static void runPythonModel(String csvPath, Path moduleRoot) throws IOException, InterruptedException {
        String pythonExe = findPythonExecutable();
        System.out.println("Using Python: " + pythonExe);
        debugLog("Using Python: " + pythonExe);

        // Try a list of likely candidate locations for the Python script (resolved against module root)
        String[] candidates = new String[] {
                "src/main/java/com/demo/Model/Model/main.py",
                "demo/src/main/java/com/demo/Model/Model/main.py",
                "target/classes/com/demo/Model/Model/main.py",
                "src/main/java/com/demo/Model/main.py",
                "Model/main.py",
                "com/demo/Model/Model/main.py"
        };

        File scriptFile = null;
        for (String c : candidates) {
            Path cand = moduleRoot.resolve(c);
            if (Files.exists(cand)) {
                scriptFile = cand.toFile();
                break;
            }
            // also check raw path (in case someone passes absolute or wd-relative)
            if (Files.exists(Paths.get(c))) {
                scriptFile = Paths.get(c).toFile();
                break;
            }
        }

        if (scriptFile == null) {
            System.err.println("DEBUG: Working Directory = " + new File(".").getAbsolutePath());
            for (String c : candidates) {
                System.err.println("DEBUG: looked at: " + moduleRoot.resolve(c).toAbsolutePath());
                debugLog("looked at: " + moduleRoot.resolve(c).toAbsolutePath());
            }
            throw new FileNotFoundException("FILE NOT FOUND! Please check path. Searched candidates and parent folders for main.py");
        }

        System.out.println("Using Script: " + scriptFile.getAbsolutePath());
        debugLog("Using Script: " + scriptFile.getAbsolutePath());
        debugLog("CSV arg passed to Python: " + csvPath);

        // Tạo lệnh chạy Python. Set working directory to moduleRoot so Python sees consistent paths
        ProcessBuilder pb = new ProcessBuilder(
                pythonExe,
                scriptFile.getAbsolutePath(),
                "--csv",
                csvPath
        );
        pb.directory(moduleRoot.toFile());

        // Đảm bảo Python in ra tiếng Việt không bị lỗi font
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        pb.redirectErrorStream(true);

        // Log some env details
        Map<String, String> env = pb.environment();
        debugLog("ENV PATH=" + env.get("PATH"));
        debugLog("ENV PYTHONPATH=" + env.get("PYTHONPATH"));

        Process process = pb.start();

        // Đọc log từ Python in ra console Java and also append to debug log
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Python] " + line);
                debugLog("[Python] " + line);
            }
        }

        int exit = process.waitFor();
        debugLog("Python process exit code: " + exit);
        if (exit != 0) {
            throw new RuntimeException("Python exited with code " + exit);
        }
    }

    // Append a single line to the debug log (creates file if missing)
    private static synchronized void debugLog(String line) {
        try {
            String l = LocalDateTime.now() + " - " + line + System.lineSeparator();
            Files.write(Paths.get(DEBUG_LOG), l.getBytes(StandardCharsets.UTF_8), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // best-effort logging
        }
    }
}
