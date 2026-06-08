package com.demo.Crawler;

import com.demo.Crawler.Entity.Config;
import com.demo.Crawler.Entity.Post;
import com.demo.Crawler.Scraper.FacebookScraper;
import com.demo.Crawler.Scraper.Scraper;
import com.demo.Crawler.Strategy.Login.AutoLoginStrategy;
import com.demo.Crawler.Strategy.Login.LoginStrategy;
import com.demo.Crawler.Strategy.Login.ManualLoginStrategy;
import com.demo.Crawler.Strategy.Navigation.ManualNavigationStrategy;
import com.demo.Crawler.Strategy.Navigation.NavigationStrategy;
import com.demo.Crawler.Strategy.Navigation.SearchNavigationStrategy;
import com.google.gson.Gson;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import io.github.bonigarcia.wdm.WebDriverManager;

public class Main {

    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : System.getProperty("config.path");
        Config config = loadConfig(configPath);
        if (config == null) return;

        // Setup ChromeDriver automatically
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications", "--no-sandbox", "--disable-dev-shm-usage");
        
        String userAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36";
        options.addArguments("--user-agent=" + userAgent);
        
        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "Nexus 5");
        options.setExperimentalOption("mobileEmulation", mobileEmulation);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("protocol_handler.excluded_schemes.fb", false); 
        prefs.put("profile.default_content_setting_values.protocol_handlers", 2); 
        options.setExperimentalOption("prefs", prefs);
        
        WebDriver driver = null;
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Attempting to start VISIBLE Chrome session...");
            driver = new ChromeDriver(options);
            System.out.println("Chrome session started successfully.");

            LoginStrategy loginStrategy = "AUTO".equalsIgnoreCase(config.getLoginMode()) ? new AutoLoginStrategy() : new ManualLoginStrategy();
            loginStrategy.login(driver, config, scanner);

            if ("SEARCH".equalsIgnoreCase(config.getNavigationMode())) {
                List<String> keywordsToUse = new ArrayList<>();
                
                System.out.println("\n=== Keyword Selection ===");
                System.out.println("1. Use keywords from config.json");
                System.out.println("2. Enter custom keywords");
                System.out.print("Choose option (1 or 2): ");
                
                
                if (config.getKeywords() != null && !config.getKeywords().isEmpty()) {
                    keywordsToUse.addAll(config.getKeywords());
                    System.out.println("Using keywords from config: " + String.join(", ", config.getKeywords()));
                }
                
                List<Post> allScrapedPosts = new ArrayList<>();
                for (String keyword : keywordsToUse) {
                    System.out.println("\n=== Searching for: " + keyword + " ===");
                    NavigationStrategy navigationStrategy = new SearchNavigationStrategy();
                    navigationStrategy.navigate(driver, keyword, scanner);

                    Scraper scraper = new FacebookScraper();
                    List<Post> postsFromKeyword = scraper.scrape(driver, config.getpostLimitPerKeyword(), config.getplatform(), keyword);
                    allScrapedPosts.addAll(postsFromKeyword);
                }
                if (!allScrapedPosts.isEmpty()) {
                    saveResults(allScrapedPosts);
                }
            } else {
                NavigationStrategy navigationStrategy = new ManualNavigationStrategy();
                navigationStrategy.navigate(driver, null, scanner); 
                Scraper scraper = new FacebookScraper();
                List<Post> manualPosts = scraper.scrape(driver, config.getPostLimitPerKeyword(), config.getPlatform(), "MANUAL");
                if (!manualPosts.isEmpty()) {
                    saveResults(manualPosts);
                }
            }

        } catch (Exception e) {
            System.err.println("\nAn unexpected error occurred during the scraping process!");
            e.printStackTrace();
        } finally {
            if (driver != null) {
                System.out.println("Process finished. Closing the browser.");
                driver.quit();
            }
        }
    }
    
    private static Config loadConfig(String explicitPath) {
        Gson gson = new Gson();

        // If explicit path provided, prefer it
        if (explicitPath != null && !explicitPath.isBlank()) {
            File f = new File(explicitPath);
            if (f.exists()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(f);
                     java.io.InputStreamReader r = new java.io.InputStreamReader(fis, StandardCharsets.UTF_8)) {
                    return gson.fromJson(r, Config.class);
                }
                catch (IOException e) { System.err.println("Could not read config: " + e.getMessage()); return null; }
            } else {
                System.err.println("Config file not found at: " + f.getAbsolutePath());
                return null;
            }
        }

        // Search locations for config.json: working dir, classpath resource
        File configFile = new File("config.json");
        if (!configFile.exists()) {
            // try classpath resource
            try (java.io.InputStream in = Main.class.getResourceAsStream("/config.json")) {
                if (in != null) {
                    try (java.io.InputStreamReader r = new java.io.InputStreamReader(in, StandardCharsets.UTF_8)) {
                        return gson.fromJson(r, Config.class);
                    }
                }
            } catch (IOException ignored) {}

            // not found — create a sample file to help the user
            try {
                createSampleConfig(new File("config.json"));
                System.err.println("A sample config.json has been created at: " + new File("config.json").getAbsolutePath());
                System.err.println("Please edit it with your settings and re-run the program.");
            } catch (IOException e) {
                System.err.println("config.json not found and failed to create sample: " + e.getMessage());
            }
            return null;
        }

        try (java.io.FileInputStream fis = new java.io.FileInputStream(configFile);
             java.io.InputStreamReader reader = new java.io.InputStreamReader(fis, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            System.err.println("Could not read config.json: " + e.getMessage());
            return null;
        }
    }

    private static void createSampleConfig(File file) throws IOException {
        if (file.exists()) return;
        String sample = "{\n" +
                "  \"loginMode\": \"AUTO\",\n" +
                "  \"email\": \"you@example.com\",\n" +
                "  \"password\": \"yourpassword\",\n" +
                "  \"navigationMode\": \"SEARCH\",\n" +
                "  \"platform\": \"web\",\n" +
                "  \"keywords\": [\"example\", \"search\"],\n" +
                "  \"postLimitPerKeyword\": 50\n" +
                "}\n";
        try (java.io.FileWriter w = new java.io.FileWriter(file, StandardCharsets.UTF_8)) { w.write(sample); }
    }

    private static void saveResults(List<Post> posts) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dtf.format(LocalDateTime.now());
        String fileName = "results_" + timestamp + ".csv";

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new java.io.FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            writer.println("Platform,Keyword,Author,Timestamp,Reactions,Comments,Content,Image URLs,Comments Text");
            
            for (Post post : posts) {
                writer.println(post.toCsvRow());
            }
            System.out.println("\nSuccessfully saved " + posts.size() + " total posts to " + fileName + " (UTF-8 encoded)");
        } catch (IOException e) {
            System.err.println("Error saving results to CSV: " + e.getMessage());
        }
    }
}