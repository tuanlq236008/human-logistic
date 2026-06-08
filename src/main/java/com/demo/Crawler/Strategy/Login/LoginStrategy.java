package com.demo.Crawler.Strategy.Login;

import java.util.Scanner;
import org.openqa.selenium.WebDriver;

import com.demo.Crawler.Entity.Config;

public interface LoginStrategy {
    void login(WebDriver driver, Config config, Scanner scanner);
}