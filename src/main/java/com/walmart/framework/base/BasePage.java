package com.walmart.framework.base;

import com.walmart.framework.utils.HumanUtils;
import com.walmart.framework.utils.WaitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * BasePage — Abstract base class for all Page Object classes.
 * Provides common Selenium interaction methods used across all pages.
 *
 * OOP: Abstraction (hides Selenium internals), Encapsulation (protected driver),
 * Inheritance (all page classes extend this).
 *
 * @author Maniraj
 */
public abstract class BasePage {

    public static final Logger logger = LogManager.getLogger(BasePage.class);

    protected final WebDriver driver;
    protected final WaitUtils waitUtils;
    protected final Actions actions;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver, 20);
        this.actions = new Actions(driver);
    }

    protected void click(By locator) {
        logger.debug("Clicking element: {}", locator);
        WebElement element = waitUtils.waitForElementClickable(locator);
        HumanUtils.humanClick(driver, element);
    }

    protected void sendKeys(By locator, String text) {
        logger.debug("Typing '{}' into: {}", text, locator);
        WebElement element = waitUtils.waitForElementVisible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value='';", element);
        HumanUtils.shortDelay();
        HumanUtils.humanType(element, text);
    }

    protected String getText(By locator) {
        return waitUtils.waitForElementVisible(locator).getText().trim();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return waitUtils.waitForElementVisible(locator).isDisplayed();
        } catch (Exception e) {
            logger.debug("Element not displayed: {}", locator);
            return false;
        }
    }

    protected void hoverOver(By locator) {
        logger.debug("Hovering over element: {}", locator);
        WebElement element = waitUtils.waitForElementVisible(locator);
        actions.moveToElement(element).perform();
        HumanUtils.mediumDelay();
    }

    protected void jsClick(By locator) {
        logger.debug("JS-assisted click on element: {}", locator);
        WebElement element = waitUtils.waitForElementVisible(locator);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior:'smooth', block:'center'});", element);
        HumanUtils.shortDelay();
        try {
            HumanUtils.humanClick(driver, element);
        } catch (Exception e) {
            logger.debug("Human click failed, falling back to JS click.");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    protected String getAttribute(By locator, String attribute) {
        return waitUtils.waitForElementVisible(locator).getAttribute(attribute);
    }

    protected String getPageTitle() {
        return driver.getTitle();
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
