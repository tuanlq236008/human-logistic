package com.demo.Crawler.Strategy.Navigation;

import org.openqa.selenium.WebDriver;
import java.util.Scanner;

public class ManualNavigationStrategy implements NavigationStrategy {
    @Override
    public void navigate(WebDriver driver, String keyword, Scanner scanner) {
        System.out.println("\n====================== MANUAL NAVIGATION ======================");
        System.out.println("Please use the browser to navigate to the page you want to scrape.");
        System.out.println("Once the page is loaded, type '2' and press Enter to begin scraping.");
        System.out.print("Waiting for start signal (2): ");
        while (!scanner.nextLine().equals("2")) {
            System.out.print("Please type '2' to start scraping: ");
        }
    }
}