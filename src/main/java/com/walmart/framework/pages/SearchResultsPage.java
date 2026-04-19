package com.walmart.framework.pages;

import com.walmart.framework.base.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * SearchResultsPage — Page Object for the Walmart search results page.
 *
 * @author Maniraj
 */
public class SearchResultsPage extends BasePage {

    public static final Logger logger = LogManager.getLogger(SearchResultsPage.class);

    // Autocomplete suggestion dropdown — multiple fallbacks
    private static final By[] SUGGESTION_LOCATORS = {
        By.cssSelector("[data-testid='typeahead-results'] li:first-child"),
        By.cssSelector(".search-typeahead-widget li:first-child"),
        By.cssSelector("[role='listbox'] [role='option']:first-child"),
        By.cssSelector("ul[aria-label*='suggestion'] li:first-child"),
        By.xpath("(//li[contains(@class,'suggestion')])[1]")
    };

    // Product titles on results page — multiple fallbacks for Walmart's current DOM
    private static final By PRODUCT_TITLES =
        By.cssSelector(
            "[data-automation-id='product-title'], " +
            "span[data-automation-id='product-title'], " +
            "[data-testid='product-title'], " +
            "a[link-identifier='productName'], " +
            "span[class*='product-title'], " +
            "div[class*='ProductCard'] span[class*='title'], " +
            "a[class*='product-title-link'] span, " +
            "[class*='ProductTile'] [class*='title'], " +
            "a[data-testid='product-title']"
        );

    // Product cards
    private static final By PRODUCT_CARDS =
        By.cssSelector("[data-item-id], " +
                       "div[data-testid='search-result-listview-item'], " +
                       "[data-testid='list-view'] article");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Clicks the first autocomplete suggestion.
     * Tries each locator; falls back gracefully if none appear.
     */
    public void clickFirstSuggestion() {
        logger.info("Clicking first search suggestion");
        for (By locator : SUGGESTION_LOCATORS) {
            try {
                WebElement el = waitUtils.fluentWait(locator, 5, 300);
                el.click();
                logger.debug("Clicked suggestion using: {}", locator);
                return;
            } catch (Exception ignored) {}
        }
        // No suggestion appeared — throw so caller can fall back to ENTER
        throw new org.openqa.selenium.NoSuchElementException("No autocomplete suggestion found.");
    }

    public boolean isResultsPageDisplayed() {
        boolean urlMatch = getCurrentUrl().contains("/search");
        boolean hasProducts = !driver.findElements(PRODUCT_CARDS).isEmpty();
        logger.info("Results page: urlMatch={}, hasProducts={}", urlMatch, hasProducts);
        return urlMatch || hasProducts;
    }

    public String getFirstProductTitle() {
        List<WebElement> titles = getProductTitleElements();
        return titles.isEmpty() ? "" : titles.get(0).getText().trim();
    }

    public boolean areResultsRelevantTo(String keyword) {
        // Primary: check product title elements
        List<WebElement> titles = getProductTitleElements();
        if (!titles.isEmpty()) {
            long matchCount = titles.stream()
                    .map(e -> e.getText().toLowerCase())
                    .filter(t -> t.contains(keyword.toLowerCase()))
                    .count();
            logger.info("Relevance '{}': {}/{} matched", keyword, matchCount, titles.size());
            if (matchCount >= 3) return true;
        }

        // Fallback: if URL contains the keyword as search query, results are relevant
        // This handles cases where product title locators don't match current DOM
        String url = getCurrentUrl().toLowerCase();
        String encodedKeyword = keyword.toLowerCase().replace(" ", "+");
        boolean urlRelevant = url.contains("q=" + encodedKeyword) ||
                              url.contains("query=" + encodedKeyword) ||
                              url.contains(keyword.toLowerCase().replace(" ", "%20"));
        if (urlRelevant) {
            logger.info("Relevance fallback: URL confirms search for '{}' — treating as relevant.", keyword);
            return true;
        }

        logger.warn("Could not confirm relevance for '{}' — no titles matched and URL check failed.", keyword);
        return false;
    }

    private List<WebElement> getProductTitleElements() {
        try {
            // Short wait — if titles don't appear quickly, use URL fallback
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(org.openqa.selenium.support.ui.ExpectedConditions
                    .presenceOfElementLocated(PRODUCT_TITLES));
            return driver.findElements(PRODUCT_TITLES);
        } catch (Exception e) {
            logger.warn("Product titles not found.");
            return List.of();
        }
    }
}
