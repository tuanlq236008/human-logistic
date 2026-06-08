package com.demo.Crawler.Strategy.Navigation;

import java.util.Scanner;
import org.openqa.selenium.WebDriver;

public interface NavigationStrategy {
    void navigate(WebDriver driver, String keyword, Scanner scanner);
}