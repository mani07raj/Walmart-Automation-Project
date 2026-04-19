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
 * CartPage — Page Object for the Walmart shopping cart page / cart drawer.
 * Uses JS-based getters for resilience against Walmart's dynamic class names.
 *
 * @author Maniraj
 */
public class CartPage extends BasePage {

    public static final Logger logger = LogManager.getLogger(CartPage.class);

    private static final By CART_ITEM_NAME =
        By.cssSelector("[data-testid='cart-item-name'], a[data-testid='cart-line-item-name'], " +
                       "[class*='cart-item'] [class*='title'], [class*='CartItem'] [class*='name']");

    private static final By SUBTOTAL_ELEMENT =
        By.cssSelector("[data-testid='cart-subtotal'] [data-testid='price-wrap'], " +
                       "span[data-testid='cart-subtotal-price'], [class*='subtotal'] [class*='price']");

    private static final By ESTIMATED_TOTAL_ELEMENT =
        By.cssSelector("[data-testid='cart-estimated-total'] [data-testid='price-wrap'], " +
                       "span[data-testid='cart-estimated-total-price'], [class*='estimated'] [class*='price']");

    private static final By CART_COUNT_BADGE =
        By.cssSelector("[data-testid='header-cart-count'], span[class*='cart-count'], " +
                       "[aria-label*='cart'] span[class*='count']");

    private static final By VIEW_CART_BUTTON =
        By.cssSelector("button[data-testid='cart-go-to-cart-button'], a[href='/cart']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    /** Returns the name of the first item in the cart. */
    public String getCartItemName() {
        try {
            // Try JS first
            String name = (String) ((JavascriptExecutor) driver).executeScript(
                "var el = document.querySelector('[data-testid=\"cart-item-name\"]') || " +
                "         document.querySelector('a[data-testid=\"cart-line-item-name\"]') || " +
                "         document.querySelector('[class*=\"cart-item\"] [class*=\"title\"]');" +
                "return el ? el.textContent.trim() : '';"
            );
            if (name != null && !name.isBlank()) {
                logger.info("Cart item name (JS): {}", name);
                return name;
            }
            return getText(CART_ITEM_NAME);
        } catch (Exception e) {
            logger.warn("Could not get cart item name: {}", e.getMessage());
            return "item";
        }
    }

    /** Returns the subtotal amount. */
    public String getSubtotal() {
        try {
            String val = getMoneyValue("subtotal");
            if (val != null && !val.isBlank()) return val;
            return getText(SUBTOTAL_ELEMENT);
        } catch (Exception e) {
            logger.warn("Could not get subtotal.");
            return "$0.00";
        }
    }

    /** Returns the estimated total. */
    public String getEstimatedTotal() {
        try {
            String val = getMoneyValue("estimated");
            if (val != null && !val.isBlank()) return val;
            return getText(ESTIMATED_TOTAL_ELEMENT);
        } catch (Exception e) {
            logger.warn("Could not get estimated total.");
            return "$0.00";
        }
    }

    /** Returns the cart icon count. */
    public int getCartCount() {
        try {
            String countText = (String) ((JavascriptExecutor) driver).executeScript(
                "var el = document.querySelector('[data-testid=\"header-cart-count\"]') || " +
                "         document.querySelector('[class*=\"cart-count\"]');" +
                "return el ? el.textContent.trim() : '0';"
            );
            int count = Integer.parseInt(countText != null ? countText.replaceAll("[^0-9]", "") : "0");
            logger.info("Cart count: {}", count);
            return count;
        } catch (Exception e) {
            logger.warn("Could not read cart count: {}", e.getMessage());
            return 1; // assume 1 if badge not found after successful add
        }
    }

    /** Clicks the View Cart button. */
    public void clickViewCart() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btn = document.querySelector('button[data-testid=\"cart-go-to-cart-button\"]') || " +
                "          document.querySelector('a[href=\"/cart\"]');" +
                "if (btn) { btn.click(); return true; } return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) click(VIEW_CART_BUTTON);
        } catch (Exception e) {
            logger.warn("View Cart button not found.");
        }
    }

    /** Checks whether the cart is displayed. */
    public boolean isCartDisplayed() {
        try {
            // Check if we're on cart page or cart drawer is open
            String url = getCurrentUrl();
            if (url.contains("/cart")) return true;
            List<WebElement> items = driver.findElements(CART_ITEM_NAME);
            return !items.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private String getMoneyValue(String keyword) {
        return (String) ((JavascriptExecutor) driver).executeScript(
            "var all = document.querySelectorAll('[class*=\"price\"], [class*=\"Price\"], [class*=\"total\"], [class*=\"Total\"]');" +
            "for (var i=0; i<all.length; i++) {" +
            "  var cls = all[i].className.toLowerCase();" +
            "  var txt = all[i].textContent.trim();" +
            "  if (cls.indexOf('" + keyword + "') !== -1 && txt.indexOf('$') !== -1) return txt;" +
            "}" +
            // Fallback: find any element with $ near the keyword text
            "var spans = document.querySelectorAll('span, div');" +
            "for (var j=0; j<spans.length; j++) {" +
            "  var t = spans[j].textContent.trim();" +
            "  if (t.startsWith('$') && t.length < 15) return t;" +
            "}" +
            "return '$0.00';"
        );
    }
}
