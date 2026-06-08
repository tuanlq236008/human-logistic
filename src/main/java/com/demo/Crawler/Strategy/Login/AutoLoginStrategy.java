package com.demo.Crawler.Strategy.Login;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.demo.Crawler.Entity.Config;

import java.time.Duration;
import java.util.Scanner;

public class AutoLoginStrategy implements LoginStrategy {
    
    @Override
    public void login(WebDriver driver, Config config, Scanner scanner) {
        System.out.println("\n=========================== AUTO LOGIN ===========================");
        System.out.println("Attempting to log in as " + config.getemail());
        
        driver.get("https://m.facebook.com/login.php");
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("m_login_email")));
            emailInput.click();
            emailInput.sendKeys(config.getemail());
            System.out.println("Entered email.");

            WebElement passwordInput = driver.findElement(By.id("m_login_password"));
            passwordInput.click();
            passwordInput.sendKeys(config.getpassword());
            System.out.println("Entered password.");
            
            try {
                driver.findElement(By.cssSelector("div[role='button'][aria-label='Log in']")).click();
            } catch (Exception e) {
                driver.findElement(By.name("login")).click();
            }
            System.out.println("Login form submitted.");
            
            try {
                System.out.println("Waiting 3 seconds to detect potential dialogs...");
                Thread.sleep(3000);
                WebElement loginButton = driver.findElement(By.cssSelector("div[role='button'][aria-label='Log in']"));
                if (loginButton.isDisplayed()) {
                    System.out.println("Login button is still visible. Assuming xdg-open dialog is active.");
                    System.out.println("Attempting to close dialog by sending ESCAPE key...");
                    new Actions(driver).sendKeys(Keys.ESCAPE).perform();
                    Thread.sleep(1000);
                    System.out.println("Re-clicking login button...");
                    loginButton.click();
                }
            } catch (Exception e) {
                System.out.println("Login successful, page has navigated.");
            }
            try {
                System.out.println("Waiting for potential 'Save Device' popup (5 seconds)...");
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement notNowButton = shortWait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[role='button'][aria-label='Lúc khác']")));
                System.out.println("'Save Device' popup found. Clicking 'Not Now' (Lúc khác)...");
                notNowButton.click();
            } catch (Exception e) {
                System.out.println("No 'Save Device' popup appeared. Continuing.");
            }
            
            Thread.sleep(3000);

        } catch (Exception e) {
            System.err.println("A critical error occurred during auto-login: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Auto-login failed. Falling back to manual.");
            new ManualLoginStrategy().login(driver, config, scanner);
        }
    }
}