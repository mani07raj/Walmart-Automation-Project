package com.walmart.framework.pages;

import com.walmart.framework.base.BasePage;
import com.walmart.framework.utils.HumanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * HomePage — Page Object for the Walmart homepage.
 * Locators include multiple fallbacks to handle dynamic UI changes.
 *
 * @author Maniraj
 */
public class HomePage extends BasePage {

    public static final Logger logger = LogManager.getLogger(HomePage.class);

    private static final By[] SEARCH_BOX_LOCATORS = {
        By.cssSelector("input[data-automation-id='header-input-search']"),
        By.cssSelector("input[name='q'][type='search']"),
        By.cssSelector("input[name='query']"),
        By.cssSelector("input[type='search']"),
        By.cssSelector("input[aria-label='Search']"),
        By.xpath("//input[@data-automation-id='header-input-search']")
    };

    private static final By[] TOYS_HOVER_LOCATORS = {
        By.cssSelector("a[aria-label='Toys & Outdoor Play']"),
        By.xpath("//a[contains(normalize-space(),'Toys') and contains(normalize-space(),'Outdoor')]"),
        By.cssSelector("a[href*='toys-games']"),
        By.cssSelector("a[href*='toys_and_outdoor']"),
        By.xpath("//li[contains(.,'Toys')]//a")
    };

    private static final By[] ALL_TOYS_LOCATORS = {
        By.cssSelector("a[aria-label*='All Toys']"),
        By.xpath("//a[contains(normalize-space(),'All Toys')]"),
        By.cssSelector("a[href*='kp=toys_and_outdoor_play']"),
        By.cssSelector("a[href*='toys'][href*='all']")
    };

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void searchFor(String keyword) {
        logger.info("Searching for: {}", keyword);
        dismissOverlays();
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var input = document.querySelector(\"input[data-automation-id='header-input-search']\");" +
            "if (!input) input = document.querySelector(\"input[name='q']\");" +
            "if (!input) input = document.querySelector(\"input[type='search']\");" +
            "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "setter.call(input, '');" +
            "input.dispatchEvent(new Event('input', {bubbles:true}));" +
            "setter.call(input, arguments[0]);" +
            "input.dispatchEvent(new Event('input', {bubbles:true}));" +
            "input.dispatchEvent(new KeyboardEvent('keydown', {key:'Enter', code:'Enter', keyCode:13, bubbles:true}));",
            keyword
        );
        logger.info("Search submitted for: {}", keyword);
        HumanUtils.mediumDelay();
    }

    public WebElement getSearchBox() {
        dismissOverlays();
        return findFirstPresent(SEARCH_BOX_LOCATORS, "Search box");
    }

    public void clickDepartments() {
        logger.info("Clicking Departments menu");
        org.openqa.selenium.By[] locators = {
            By.cssSelector("button[aria-label='Departments']"),
            By.xpath("//button[normalize-space()='Departments']"),
            By.xpath("//a[normalize-space()='Departments']"),
            By.cssSelector("[data-testid='departments-button']"),
            By.xpath("//*[contains(@class,'dept') and (self::button or self::a)]"),
            By.cssSelector("nav a[href*='departments'], nav button[class*='department']")
        };
        for (org.openqa.selenium.By loc : locators) {
            try {
                List<WebElement> els = driver.findElements(loc);
                if (!els.isEmpty() && els.get(0).isDisplayed()) {
                    ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", els.get(0));
                    logger.info("Clicked Departments using: {}", loc);
                    return;
                }
            } catch (Exception ignored) {}
        }
        try {
            WebElement el = driver.findElement(
                By.xpath("//*[normalize-space(text())='Departments' or normalize-space(text())='Departments ']"));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            logger.info("Clicked Departments via text XPath.");
        } catch (Exception e) {
            logger.warn("Departments button not found: {}", e.getMessage());
        }
    }

    public void hoverOnToysAndOutdoorPlay() {
        logger.info("Hovering over Toys & Outdoor Play");
        WebElement el = findFirstPresent(TOYS_HOVER_LOCATORS, "Toys hover link");
        actions.moveToElement(el).perform();
        HumanUtils.mediumDelay();
    }

    public void clickAllToysAndOutdoorPlay() {
        logger.info("Clicking All Toys & Outdoor Play");
        WebElement el = findFirstPresent(ALL_TOYS_LOCATORS, "All Toys link");
        jsClickElement(el);
    }

    public boolean isHomePageLoaded() {
        return getPageTitle().contains("Walmart");
    }

    private void dismissOverlays() {
        try {
            driver.findElement(By.tagName("body")).sendKeys(org.openqa.selenium.Keys.ESCAPE);
            HumanUtils.shortDelay();
        } catch (Exception ignored) {}
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "var patterns = ['ModalPortal_container','ModalPortal_scrim'," +
                "  'OverlayScrim_scrim','BottomSheet_container','Overlay_overlay'];" +
                "patterns.forEach(function(p) {" +
                "  document.querySelectorAll('[class*=\"'+p+'\"]').forEach(function(el){el.remove();});" +
                "});" +
                "document.body.style.overflow='auto';"
            );
        } catch (Exception ignored) {}
        HumanUtils.shortDelay();
    }

    private WebElement findFirstPresent(By[] locators, String elementName) {
        for (By locator : locators) {
            try {
                List<WebElement> found = driver.findElements(locator);
                if (!found.isEmpty() && found.get(0).isDisplayed()) {
                    logger.debug("Found '{}' using: {}", elementName, locator);
                    return found.get(0);
                }
            } catch (Exception ignored) {}
        }
        logger.warn("No match for '{}', waiting on primary locator.", elementName);
        return waitUtils.waitForElementClickable(locators[0]);
    }

    private void jsClickElement(WebElement el) {
        ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        HumanUtils.shortDelay();
        ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].click();", el);
    }
}
