package com.walmart.framework.browser;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * BrowserFactory — Creates WebDriver instances for the configured browser.
 * Implements IBrowser and supports Chrome, Edge, and Firefox.
 *
 * @author Maniraj
 */
public class BrowserFactory implements IBrowser {

    public static final Logger logger = LogManager.getLogger(BrowserFactory.class);

    private static final String NAVIGATOR_OVERRIDES =
        "Object.defineProperty(navigator, 'webdriver',  {get: () => undefined});" +
        "Object.defineProperty(navigator, 'plugins',    {get: () => [1,2,3,4,5]});" +
        "Object.defineProperty(navigator, 'languages',  {get: () => ['en-US','en']});" +
        "window.chrome = { runtime: {} };";

    @Override
    public WebDriver createBrowserInstance(String browserName) {
        WebDriver driver;

        switch (browserName.toLowerCase().trim()) {
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver(buildEdgeOptions());
            }
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver(buildFirefoxOptions());
            }
            default -> {
                System.setProperty("webdriver.chrome.driver",
                    System.getProperty("user.home") +
                    "\\.cache\\selenium\\chromedriver\\win64\\147.0.7727.57\\chromedriver.exe");
                driver = new ChromeDriver(buildChromeOptions());
            }
        }

        try {
            ((JavascriptExecutor) driver).executeScript(NAVIGATOR_OVERRIDES);
        } catch (Exception ignored) {}

        driver.manage().window().maximize();
        logger.info("Browser launched: {}", browserName);
        return driver;
    }

    private EdgeOptions buildEdgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("start-maximized");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments(
            "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36 Edg/147.0.0.0"
        );
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);
        return options;
    }

    private ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("start-maximized");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments(
            "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36"
        );
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);
        return options;
    }

    private FirefoxOptions buildFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920", "--height=1080");
        options.addPreference("dom.webdriver.enabled", false);
        options.addPreference("useAutomationExtension", false);
        return options;
    }
}
