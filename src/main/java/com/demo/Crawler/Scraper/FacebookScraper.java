package com.demo.Crawler.Scraper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.demo.Crawler.Entity.Post;

public class FacebookScraper implements Scraper {
    private static final String POST_CONTAINER_SELECTOR = "div[data-tracking-duration-id]";

    @Override
    public List<Post> scrape(WebDriver driver, int postLimit, String platform, String keyword) {
        System.out.println("\n================== SCRAPING (Limit: " + postLimit + " posts for keyword: '" + keyword + "') =================");
        List<Post> scrapedPosts = new ArrayList<>();
        int processedPostIndex = 0;
        int consecutiveScrolls = 0;

        while (scrapedPosts.size() < postLimit && processedPostIndex < (scrapedPosts.size() + 20) ) {
            try {
                 new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(POST_CONTAINER_SELECTOR)));
            } catch (Exception e) {
                System.out.println("Waited 10 seconds, but no posts were found. Moving to next keyword.");
                break;
            }
            
            List<WebElement> allPostsOnPage = driver.findElements(By.cssSelector(POST_CONTAINER_SELECTOR));

            if (allPostsOnPage.size() > processedPostIndex) {
                consecutiveScrolls = 0;
                WebElement postToProcess = allPostsOnPage.get(processedPostIndex);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", postToProcess);
                try { Thread.sleep(500); } catch (InterruptedException ex) {}

                System.out.println("\n--- Processing Post [" + (processedPostIndex + 1) + "] ---");
                Post postData = scrapePostDetails(driver, postToProcess);
                if (postData.isScrapeSuccessful()) {
                    postData.setPlatform(platform);
                    postData.setKeyword(keyword);
                    System.out.println(postData.toString());
                    scrapedPosts.add(postData);
                } else {
                    System.err.println("   -> SCRAPE FAILED for this post. Skipping.");
                }
                processedPostIndex++;
            } else {
                System.out.println("\nNo new posts at current index. Scrolling down...");
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
                try { Thread.sleep(3000); } catch (InterruptedException e) {}

                consecutiveScrolls++;
                if (consecutiveScrolls >= 3) {
                    System.out.println("Scrolled multiple times with no new posts. Assuming end of feed for this keyword.");
                    break;
                }
            }
        }
        System.out.println("\nScraping for keyword '" + keyword + "' complete. Total posts found: " + scrapedPosts.size());
        return scrapedPosts;
    }

    @SuppressWarnings("deprecation")
    private Post scrapePostDetails(WebDriver driver, WebElement postContainer) {
        Post post = new Post();
        String originalId = postContainer.getAttribute("data-tracking-duration-id");
        if (originalId == null) { return new Post(); }

        try {
            WebElement freshPostContainer = postContainer;
            
            try {
                WebElement seeMoreSpan = freshPostContainer.findElement(By.xpath(".//*[contains(text(), 'Xem thêm') or contains(text(), 'See more')]"));
                
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", seeMoreSpan);
                
                System.out.println("   -> Surgically clicked 'See more'.");
                Thread.sleep(1000); 
                
                freshPostContainer = driver.findElement(By.cssSelector("div[data-tracking-duration-id='" + originalId + "']"));
            } catch (Exception e) { 
            }
            
            try {
                String fullAuthorText = "";
                WebElement authorBlock = freshPostContainer.findElement(By.cssSelector("div[role='button'][aria-label='Tap to open profile page']"));
                List<WebElement> authorSpans = authorBlock.findElements(By.cssSelector("span[role='link']"));
                if (!authorSpans.isEmpty()) {
                    StringBuilder authorBuilder = new StringBuilder();
                    for (WebElement span : authorSpans) { 
                        authorBuilder.append(span.getText().replace("\n", " ")).append(" ► "); 
                    }
                    fullAuthorText = authorBuilder.toString();
                }
                if (fullAuthorText.contains(" ► ")) { 
                    post.setAuthor(fullAuthorText.split(" ► ")[0].trim()); 
                } else { 
                    post.setAuthor(fullAuthorText.trim()); 
                }
            } catch(Exception e) { System.err.println("Could not find author."); }
            
            // Timestamp
            try { post.setTimePosted(freshPostContainer.findElement(By.cssSelector("span.f5")).getText()); } catch (Exception e) { System.err.println("Could not find timestamp."); }
            
            // Robust, multi-selector content logic
            try {
                 List<By> contentSelectors = new ArrayList<>();
                 contentSelectors.add(By.cssSelector("div[data-action-id] div[dir='auto']"));
                 contentSelectors.add(By.cssSelector("span.f4"));
                 contentSelectors.add(By.cssSelector("div[data-mcomponent='TextArea'] div[dir='auto']"));
                 for (By selector : contentSelectors) {
                     try {
                         List<WebElement> elements = freshPostContainer.findElements(selector);
                         if (!elements.isEmpty()) {
                             StringBuilder sb = new StringBuilder();
                             for (WebElement el : elements) {
                                 String text = el.getText();
                                 if (text != null && !text.contains("►") && !text.matches("(?i).*see translation.*")) {
                                     sb.append(text).append("\n");
                                 }
                             }
                             String combined = sb.toString().trim();
                             if (!combined.isEmpty()) { post.setContent(combined); break; }
                         }
                     } catch (NoSuchElementException ex) { /* Ignore */ }
                 }
            } catch (Exception e) { System.err.println("Could not find content."); }

            try { 
                List<WebElement> i = freshPostContainer.findElements(By.cssSelector("div[aria-label*='image'] img")); 
                for (WebElement img : i) { 
                    post.getImageUrls().add(img.getAttribute("src")); 
                } 
            } 
            catch (Exception e) { System.err.println("Error scraping images."); }

            try { 
                WebElement r = freshPostContainer.findElement(By.cssSelector("div[aria-label*='reacted']")).findElement(By.cssSelector("span.f1")); 
                post.setReactionsCount(r.getText()); 
            } 
            catch (Exception e) { System.err.println("Could not find reaction count."); }

            try { 
                post.setCommentsCount(freshPostContainer.findElement(By.cssSelector("div[aria-label*='comment']")).getText()); 
            } 
            catch (Exception e) { System.err.println("Could not find comment count."); }
            
            // Scrape comments
            try {
                post.setComments(scrapeComments(driver, freshPostContainer));
            } catch (Exception e) {
                System.err.println("Could not scrape comments: " + e.getMessage());
            }
        
        } catch (StaleElementReferenceException e) {
            System.out.println("   -> Stale element detected. Retrying...");
            try {
                WebElement freshRetryContainer = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-tracking-duration-id='" + originalId + "']")));
                return scrapePostDetails(driver, freshRetryContainer);
            } catch (Exception retryException) {
                System.err.println("   -> FAILED to re-find post. Skipping.");
                return new Post();
            }
        }
        return post;
    }

    private List<String> scrapeComments(WebDriver driver, WebElement postContainer) {
        List<String> comments = new ArrayList<>();
        
        try {
            // Try to click on "View more comments" or similar buttons
            try {
                List<WebElement> viewMoreButtons = postContainer.findElements(By.xpath(".//*[contains(text(), 'View') and (contains(text(), 'comment') or contains(text(), 'reply'))]"));
                for (WebElement button : viewMoreButtons) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        // Ignore click failures
                    }
                }
            } catch (Exception e) {
                // Ignore if no view more buttons
            }
            
            // Find comment containers
            List<WebElement> commentElements = postContainer.findElements(By.cssSelector("div[data-visualcompletion='ignore-dynamic'] div[dir='auto'], div[role='article'] div[dir='auto']"));
            
            for (WebElement commentElement : commentElements) {
                try {
                    String commentText = commentElement.getText();
                    if (commentText != null && !commentText.trim().isEmpty() && commentText.length() > 10) {
                        // Clean up the comment text
                        commentText = commentText.trim();
                        // Skip if it looks like a timestamp or reaction count
                        if (!commentText.matches("\\d+.*") && !commentText.toLowerCase().contains("like") && !commentText.toLowerCase().contains("reply")) {
                            comments.add(commentText);
                        }
                    }
                } catch (Exception e) {
                    // Skip problematic comments
                }
            }
            
            // Alternative approach: look for comments in the comment section
            if (comments.isEmpty()) {
                try {
                    WebElement commentsSection = postContainer.findElement(By.xpath(".//div[contains(@aria-label, 'comment') or contains(@data-pagelet, 'Comment')]"));
                    List<WebElement> commentTexts = commentsSection.findElements(By.cssSelector("div[dir='auto']"));
                    
                    for (WebElement commentText : commentTexts) {
                        String text = commentText.getText();
                        if (text != null && !text.trim().isEmpty() && text.length() > 5) {
                            comments.add(text.trim());
                        }
                    }
                } catch (Exception e) {
                    // Ignore if alternative approach fails
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error scraping comments: " + e.getMessage());
        }
        
        return comments;
    }
}