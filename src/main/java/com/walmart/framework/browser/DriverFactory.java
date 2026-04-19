package com.walmart.framework.browser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * DriverFactory — Singleton WebDriver manager using ThreadLocal for thread safety.
 *
 * Implements the Singleton pattern to ensure a single instance per application lifecycle.
 * ThreadLocal<WebDriver> provides isolated driver instances per thread for parallel execution.
 *
 * @author Maniraj
 */
public class DriverFactory {

    public static final Logger logger = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {}

    private static DriverFactory instance;

    public static DriverFactory getInstance() {
        if (instance == null) {
            instance = new DriverFactory();
        }
        return instance;
    }

    private final ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public WebDriver getDriver() {
        return tlDriver.get();
    }

    public void setDriver(WebDriver driverParam) {
        tlDriver.set(driverParam);
    }

    public void closeDriver() {
        WebDriver driver = tlDriver.get();
        if (driver != null) {
            driver.quit();
            tlDriver.remove();
            logger.info("WebDriver closed and removed from ThreadLocal.");
        }
    }
}
