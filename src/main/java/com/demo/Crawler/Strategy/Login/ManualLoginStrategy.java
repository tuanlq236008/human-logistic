package com.demo.Crawler.Strategy.Login;

import org.openqa.selenium.WebDriver;

import com.demo.Crawler.Entity.Config;

import java.util.Scanner;

public class ManualLoginStrategy implements LoginStrategy {
    @Override
    public void login(WebDriver driver, Config config, Scanner scanner) {
        driver.get("https://m.facebook.com");
        System.out.println("\n=========================== MANUAL LOGIN ===========================");
        System.out.println("Please log into Facebook in the browser window.");
        System.out.println("After you are logged in, come back here, type '1', and press Enter.");
        System.out.print("Waiting for login confirmation (1): ");
        while (!scanner.nextLine().equals("1")) {
            System.out.print("Please type '1' to confirm: ");
        }
    }
}