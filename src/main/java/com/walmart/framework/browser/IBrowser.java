package com.walmart.framework.browser;

import org.openqa.selenium.WebDriver;

/**
 * IBrowser — Interface defining the contract for browser instance creation.
 *
 * @author Maniraj
 */
public interface IBrowser {

    /**
     * Creates and returns a configured WebDriver instance for the given browser.
     *
     * @param browserName the browser to launch (chrome | edge | firefox)
     * @return a ready-to-use WebDriver
     */
    WebDriver createBrowserInstance(String browserName);
}
