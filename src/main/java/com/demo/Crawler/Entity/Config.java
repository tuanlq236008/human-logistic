package com.demo.Crawler.Entity;

import java.util.List;

//Please change the config in the config.xml, not this.
/*
- CRAWLER'S CONFIG FILE:
+ loginMode: "MANUAL"/"AUTO":
	+ Manual: Enter it by yourself yes
	+ Auto: Please enter the "email" and the "password" field so it login automatically by itself
+ email: Email to login automatically. loginMode need to be set to "AUTO"
+ password: Password to login automatically. loginMode need to be set to "AUTO"
+ navigationMode: "MANUAL"/"SEARCH"
	+ Manual: Go to the website you want to crawl all the posts, and run the program. It will scroll down by itself, press See more (if possible) and copy the post info
	+ Search: Enter the search terms in "searchTerm" to search and scrape automatically
+ searchTerm: Search terms to be searched. navigationMode need to be set to "AUTO"
+ postLimit: Maximum number of post(s) to be scraped.
+ driverPaths: The localion of browser's driver path. For example, Chrome: https://googlechromelabs.github.io/chrome-for-testing/
*/

public class Config {
    private String loginMode;
    String email;
    String password;
    String navigationMode;
    private String platform;
    List<String> keywords;
    private int postLimitPerKeyword;

    public String getloginMode() {
        return getLoginMode();
    }
    public String getPlatform() {
        return platform;
        
    }
    public void setPlatform(String platform) {
        this.platform = platform;
        
    }
    public int getPostLimitPerKeyword() {
        return postLimitPerKeyword;
        
    }
    public void setPostLimitPerKeyword(int postLimitPerKeyword) {
        this.postLimitPerKeyword = postLimitPerKeyword;
        
    }
    public String getLoginMode() {
        return loginMode;
        
    }
    public void setLoginMode(String loginMode) {
        this.loginMode = loginMode;
        
    }
    public String getemail() {
        return email;
    }
    public String getpassword() {
        return password;
    }
    public String getnavigationMode() {
        return navigationMode;
    }
    public String getplatform() {
        return getPlatform();
    }
    public List<String> getKeywords() {
        return keywords;
    }
    public int getpostLimitPerKeyword() {
        return getPostLimitPerKeyword();
    }       

    public void setemail(String email) {
        this.email = email;
    }
    
    public void setpassword(String password) {
        this.password = password;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    public String getNavigationMode() {
        return navigationMode;
    }
}