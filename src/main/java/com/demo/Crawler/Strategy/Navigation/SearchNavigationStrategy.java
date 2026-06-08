package com.demo.Crawler.Strategy.Navigation;

import org.openqa.selenium.WebDriver;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SearchNavigationStrategy implements NavigationStrategy {

    @Override
    public void navigate(WebDriver driver, String keyword, Scanner scanner) {
        System.out.println("\n====================== SEARCH NAVIGATION ======================");
        
        if (keyword == null || keyword.trim().isEmpty()) {
            System.err.println("Keyword is empty. Cannot perform search.");
            return;
        }
        
        System.out.println("Searching for term: '" + keyword + "'");
        
        try {
            String encodedSearchTerm = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
            String searchUrl = "https://m.facebook.com/search_results/?q=" + encodedSearchTerm;
            driver.get(searchUrl);
            System.out.println("Navigation complete for keyword: " + keyword);
            
        } catch (Exception e) {
            System.err.println("An error occurred during search navigation for keyword '" + keyword + "': " + e.getMessage());
        }
    }
}