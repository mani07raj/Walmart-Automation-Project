package com.walmart.framework.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScreenshotUtils — Captures and persists screenshots on test failure.
 * Supports file-based, Base64, and byte-array output formats.
 *
 * @author Maniraj
 */
public final class ScreenshotUtils {

    public static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotUtils() {}

    /**
     * Captures a screenshot and saves it under target/screenshots/.
     *
     * @param driver       active WebDriver instance
     * @param scenarioName name of the failing scenario
     * @return absolute path of the saved file, or null on failure
     */
    public static String takeScreenshot(WebDriver driver, String scenarioName) {
        if (driver == null) {
            logger.warn("Cannot take screenshot — WebDriver is null.");
            return null;
        }
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String sanitizedName = scenarioName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String fileName = timestamp + "_" + sanitizedName + ".png";
            String screenshotDir = System.getProperty("user.dir") + File.separator + "target"
                    + File.separator + "screenshots";
            File destFile = new File(screenshotDir + File.separator + fileName);
            FileUtils.forceMkdirParent(destFile);
            FileUtils.copyFile(srcFile, destFile);
            String absolutePath = destFile.getAbsolutePath();
            logger.info("Screenshot saved: {}", absolutePath);
            return absolutePath;
        } catch (IOException e) {
            logger.error("Failed to save screenshot for scenario: {}", scenarioName, e);
            return null;
        }
    }

    public static String takeScreenshotAsBase64(WebDriver driver) {
        if (driver == null) {
            logger.warn("Cannot take screenshot — WebDriver is null.");
            return "";
        }
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot as Base64.", e);
            return "";
        }
    }

    public static byte[] takeScreenshotAsBytes(WebDriver driver) {
        if (driver == null) return new byte[0];
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot bytes.", e);
            return new byte[0];
        }
    }
}
