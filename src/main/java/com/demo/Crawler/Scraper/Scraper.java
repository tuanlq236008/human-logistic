package com.demo.Crawler.Scraper;

import java.util.List;
import org.openqa.selenium.WebDriver;

import com.demo.Crawler.Entity.Post;

public interface Scraper {
    List<Post> scrape(WebDriver driver, int postLimit, String platform, String keyword);
}