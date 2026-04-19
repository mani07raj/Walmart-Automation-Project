package com.walmart.framework.pages;

import com.walmart.framework.base.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * ProductListingPage — Page Object for the product listing / category page.
 *
 * Represents the page shown after selecting a department category,
 * displaying a grid or list of products to choose from.
 *
 * @author Maniraj
 */
public class ProductListingPage extends BasePage {

    public static final Logger logger = LogManager.getLogger(ProductListingPage.class);

    // ─── Private Locators ────────────────────────────────────────────────────

    /** All product items in the listing grid */
    private static final By PRODUCT_LIST =
            By.cssSelector("[data-testid='list-view'] [data-item-id], " +
                           "[data-testid='item-stack'] article, " +
                           "div[data-testid='search-result-listview-item']");

    /** Product title within each listing card */
    private static final By PRODUCT_TITLE_IN_CARD =
            By.cssSelector("[data-automation-id='product-title']");

    public ProductListingPage(WebDriver driver) {
        super(driver);
    }

    // ─── Public Business Methods ──────────────────────────────────────────────

    /**
     * Clicks on the first product in the listing using JS link traversal.
     * More reliable than CSS selectors on Walmart's dynamic product grid.
     */
    public void selectProduct(int index) {
        logger.info("Selecting product at index: {}", index);
        com.walmart.framework.utils.HumanUtils.waitForPageLoadAndPause(driver);

        // JS: find all product links and click the one at the given index
        Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
            "var items = document.querySelectorAll(" +
            "  '[data-item-id] a[href*=\"/ip/\"], " +
            "   article a[href*=\"/ip/\"], " +
            "   [data-testid*=\"item\"] a[href*=\"/ip/\"]'" +
            ");" +
            "var idx = arguments[0];" +
            "if (items.length > idx) { items[idx].click(); return true; }" +
            "if (items.length > 0)   { items[0].click();   return true; }" +
            "return false;",
            index
        );

        if (!Boolean.TRUE.equals(clicked)) {
            // Fallback: click any /ip/ link on the page
            logger.warn("Product grid links not found — trying any /ip/ link.");
            ((JavascriptExecutor) driver).executeScript(
                "var link = document.querySelector('a[href*=\"/ip/\"]');" +
                "if (link) link.click();"
            );
        }
        com.walmart.framework.utils.HumanUtils.waitForPageLoadAndPause(driver);
    }

    /**
     * Returns the display name of the product at the given index.
     */
    public String getProductName(int index) {
        try {
            // Try JS to get product title text
            String name = (String) ((JavascriptExecutor) driver).executeScript(
                "var titles = document.querySelectorAll(" +
                "  '[data-automation-id=\"product-title\"], " +
                "   [data-testid=\"product-title\"], " +
                "   [class*=\"product-title\"]'" +
                ");" +
                "return titles.length > arguments[0] ? titles[arguments[0]].textContent.trim() : '';",
                index
            );
            if (name != null && !name.isBlank()) return name;
        } catch (Exception ignored) {}
        logger.warn("Cannot get product name at index {}", index);
        return "Product " + index;
    }

    /** Returns the total number of products visible on the listing page. */
    public int getProductCount() {
        return getProductList().size();
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private List<WebElement> getProductList() {
        try {
            waitUtils.waitForElementVisible(PRODUCT_LIST);
            return driver.findElements(PRODUCT_LIST);
        } catch (Exception e) {
            logger.warn("Product list not found on listing page.");
            return List.of();
        }
    }

    /** JS click on a WebElement directly (for list items). */
    private void jsClickElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        js.executeScript("arguments[0].click();", element);
    }
}
