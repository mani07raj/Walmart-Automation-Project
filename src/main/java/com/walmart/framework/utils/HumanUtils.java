package com.walmart.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.Random;

/**
 * HumanUtils — Utility class for introducing realistic interaction timing.
 * Adds randomised delays, incremental typing, and natural mouse movement
 * to reduce the likelihood of automated session detection.
 *
 * @author Maniraj
 */
public final class HumanUtils {

    public static final Logger logger = LogManager.getLogger(HumanUtils.class);
    private static final Random RANDOM = new Random();

    private HumanUtils() {}

    public static void randomDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + RANDOM.nextInt(maxMs - minMs);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void shortDelay() {
        randomDelay(100, 300);
    }

    public static void mediumDelay() {
        randomDelay(400, 900);
    }

    public static void longDelay() {
        randomDelay(1000, 2500);
    }

    /**
     * Types each character with a randomised inter-key delay to simulate natural typing speed.
     */
    public static void humanType(WebElement element, String text) {
        element.click();
        shortDelay();
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            randomDelay(80, 200);
        }
        logger.debug("Human-typed: '{}'", text);
    }

    /**
     * Moves the mouse to the element with a slight random offset before clicking.
     */
    public static void humanClick(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.moveToElement(element, RANDOM.nextInt(5), RANDOM.nextInt(5))
               .pause(java.time.Duration.ofMillis(randomBetween(100, 300)))
               .click()
               .perform();
        shortDelay();
        logger.debug("Human-clicked element.");
    }

    /**
     * Scrolls the page down incrementally to simulate a user scanning content.
     */
    public static void humanScroll(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int totalScroll = randomBetween(200, 500);
        int steps = randomBetween(3, 6);
        int perStep = totalScroll / steps;
        for (int i = 0; i < steps; i++) {
            js.executeScript("window.scrollBy(0, " + perStep + ")");
            randomDelay(80, 180);
        }
        logger.debug("Human-scrolled {} px in {} steps.", totalScroll, steps);
    }

    public static void scrollToTop(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo({top: 0, behavior: 'smooth'})");
        shortDelay();
    }

    /**
     * Waits for document.readyState to be 'complete', then pauses briefly
     * before the first interaction.
     */
    public static void waitForPageLoadAndPause(WebDriver driver) {
        long start = System.currentTimeMillis();
        long timeout = 15_000;
        while (System.currentTimeMillis() - start < timeout) {
            String state = (String) ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState");
            if ("complete".equals(state)) break;
            randomDelay(200, 400);
        }
        longDelay();
    }

    private static int randomBetween(int min, int max) {
        return min + RANDOM.nextInt(max - min);
    }
}
