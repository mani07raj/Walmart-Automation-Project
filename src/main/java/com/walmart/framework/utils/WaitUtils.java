package com.walmart.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * WaitUtils — Explicit and fluent wait wrappers for Selenium 4.
 * Centralises all wait logic to avoid raw Thread.sleep() usage across the framework.
 *
 * @author Maniraj
 */
public class WaitUtils {

    public static final Logger logger = LogManager.getLogger(WaitUtils.class);

    private final WebDriver driver;
    private final int explicitWaitSeconds;

    public WaitUtils(WebDriver driver, int explicitWaitSeconds) {
        this.driver = driver;
        this.explicitWaitSeconds = explicitWaitSeconds;
    }

    public WebElement waitForElementVisible(By locator) {
        logger.debug("Waiting for element visible: {}", locator);
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElementClickable(By locator) {
        logger.debug("Waiting for element clickable: {}", locator);
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean waitForTextToBePresentIn(By locator, String text) {
        logger.debug("Waiting for text '{}' in: {}", text, locator);
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds))
                    .until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
        } catch (Exception e) {
            logger.warn("Text '{}' not found in element: {}", text, locator);
            return false;
        }
    }

    public WebElement fluentWait(By locator, int timeoutSec, int pollMs) {
        logger.debug("FluentWait for: {} | timeout={}s | poll={}ms", locator, timeoutSec, pollMs);
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(pollMs))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public boolean waitForTitleContains(String titleFragment) {
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds))
                .until(ExpectedConditions.titleContains(titleFragment));
    }

    public boolean waitForUrlContains(String urlFragment) {
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds))
                .until(ExpectedConditions.urlContains(urlFragment));
    }
}
