package com.walmart.framework.pages;

import com.walmart.framework.base.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * ProductDetailsPage — Page Object for the individual product detail page.
 *
 * @author Maniraj
 */
public class ProductDetailsPage extends BasePage {

    public static final Logger logger = LogManager.getLogger(ProductDetailsPage.class);

    // ─── Private Locators ────────────────────────────────────────────────────

    private static final By PRODUCT_TITLE =
            By.cssSelector("h1[itemprop='name'], [data-testid='product-title'], h1.prod-ProductTitle");

    private static final By PRODUCT_PRICE =
            By.cssSelector("[itemprop='price'], [data-testid='price-wrap'] span[aria-hidden='true']");

    private static final By ADD_TO_CART_BUTTON =
            By.cssSelector("button[data-testid='add-to-cart-btn'], " +
                           "button[data-automation-id='add-to-cart'], " +
                           "button[class*='add-to-cart']");

    private static final By PRODUCT_PAGE_INDICATOR =
            By.cssSelector("[data-testid='product-title'], h1[itemprop='name']");

    public ProductDetailsPage(WebDriver driver) {
        super(driver);
    }

    // ─── Public Business Methods ──────────────────────────────────────────────

    /**
     * Returns the product title text from the detail page heading.
     */
    public String getProductTitle() {
        String title = getText(PRODUCT_TITLE);
        logger.info("Product title on detail page: {}", title);
        return title;
    }

    /**
     * Returns the product price as displayed on the page.
     */
    public String getProductPrice() {
        try {
            return getText(PRODUCT_PRICE);
        } catch (Exception e) {
            logger.warn("Could not retrieve product price.");
            return "";
        }
    }

    /**
     * Clicks the "Add to cart" button using JS — bypasses any overlay.
     */
    public void clickAddToCart() {
        logger.info("Clicking Add to Cart button");
        com.walmart.framework.utils.HumanUtils.mediumDelay();
        Boolean clicked = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var btns = document.querySelectorAll(" +
            "  'button[data-testid=\"add-to-cart-btn\"]," +
            "   button[data-automation-id=\"add-to-cart\"]," +
            "   button[class*=\"add-to-cart\"]'" +
            ");" +
            "for (var i=0; i<btns.length; i++) {" +
            "  if (btns[i].offsetParent !== null) { btns[i].click(); return true; }" +
            "}" +
            // Fallback: any button with 'Add to cart' text
            "var all = document.querySelectorAll('button');" +
            "for (var j=0; j<all.length; j++) {" +
            "  if (all[j].textContent.toLowerCase().indexOf('add to cart') !== -1) {" +
            "    all[j].click(); return true;" +
            "  }" +
            "}" +
            "return false;"
        );
        if (!Boolean.TRUE.equals(clicked)) {
            logger.warn("Add to Cart button not found via JS — trying Selenium click.");
            click(ADD_TO_CART_BUTTON);
        }
        com.walmart.framework.utils.HumanUtils.mediumDelay();
    }

    /**
     * Verifies that the current page is a product detail page.
     */
    public boolean isOnProductPage() {
        boolean result = isDisplayed(PRODUCT_PAGE_INDICATOR);
        logger.info("Is on product page: {}", result);
        return result;
    }
}
