package com.walmart.tests.hooks;

import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.aventstack.extentreports.service.ExtentService;
import com.walmart.framework.browser.BrowserFactory;
import com.walmart.framework.browser.DriverFactory;
import com.walmart.framework.config.ConfigReader;
import com.walmart.framework.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * ApplicationHooks — Cucumber lifecycle hooks for browser setup and teardown.
 *
 * @Before(order=0): loads config.properties
 * @Before(order=1): launches browser, navigates to base URL, dismisses overlays
 * @After(order=1):  captures screenshot on failure and attaches to Extent report
 * @After(order=0):  quits the browser
 *
 * @author Maniraj
 */
public class ApplicationHooks {

    public static final Logger logger = LogManager.getLogger(ApplicationHooks.class);

    public static WebDriver driver;
    private ConfigReader configReader;
    Properties prop;
    protected BrowserFactory bf = new BrowserFactory();

    static LocalDateTime currentDateTime = LocalDateTime.now();

    static DateTimeFormatter dateMonthYearFormat = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    public static String dateMonthYear = currentDateTime.format(dateMonthYearFormat);

    static DateTimeFormatter twentyFourHourFormat = DateTimeFormatter.ofPattern("k-mm-ss a ");
    public static String twentyFourHour = currentDateTime.format(twentyFourHourFormat);

    static DateTimeFormatter twelveHourFormat = DateTimeFormatter.ofPattern("hh-mm-ss a ");
    public static String twelveHour = currentDateTime.format(twelveHourFormat);

    static DateTimeFormatter monthYearFormat = DateTimeFormatter.ofPattern("MMM yyyy");
    public static String monthYear = currentDateTime.format(monthYearFormat);

    static DateTimeFormatter yearFormat = DateTimeFormatter.ofPattern("yyyy");
    public static String year = currentDateTime.format(yearFormat);

    @Before(order = 0)
    public void getProperty() {
        logger.info("In Before(order=0) - Current Time: {}", twelveHour);
        configReader = new ConfigReader();
        prop = configReader.init_prop();
    }

    @Before(order = 1)
    public void launchBrowser(Scenario scenario) {
        logger.info("In Before(order=1) - Launching browser for scenario: {}", scenario.getName());
        String browserName = configReader.getBrowser();
        DriverFactory.getInstance().setDriver(bf.createBrowserInstance(browserName));
        driver = DriverFactory.getInstance().getDriver();

        String baseUrl = configReader.getBaseUrl();
        driver.get(baseUrl);

        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver',  {get: () => undefined});" +
                "Object.defineProperty(navigator, 'plugins',    {get: () => [1,2,3,4,5]});" +
                "Object.defineProperty(navigator, 'languages',  {get: () => ['en-US','en']});" +
                "window.chrome = { runtime: {} };"
            );
        } catch (Exception ignored) {}

        com.walmart.framework.utils.HumanUtils.waitForPageLoadAndPause(driver);
        com.walmart.framework.utils.HumanUtils.humanScroll(driver);
        com.walmart.framework.utils.HumanUtils.scrollToTop(driver);
        com.walmart.framework.utils.HumanUtils.mediumDelay();
        dismissPopupIfPresent(driver);
        logger.info("Navigated to base URL: {}", baseUrl);

        ExtentService.getInstance().setSystemInfo("Application", "Walmart");
        ExtentService.getInstance().setSystemInfo("User Name", System.getProperty("user.name"));
        ExtentService.getInstance().setSystemInfo("Environment", "QA");
        ExtentService.getInstance().setSystemInfo("URL", baseUrl);
        ExtentService.getInstance().setSystemInfo("OS", System.getProperty("os.name"));
        ExtentService.getInstance().setSystemInfo("Browser", browserName);
    }

    @After(order = 1)
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed() && driver != null) {
            try {
                String screenshotName = scenario.getName().replaceAll(" ", "_");

                com.aventstack.extentreports.ExtentTest step = null;
                try { step = ExtentCucumberAdapter.getCurrentStep(); } catch (Exception ignored) {}

                if (step != null) {
                    String base64 = ScreenshotUtils.takeScreenshotAsBase64(driver);
                    step.log(com.aventstack.extentreports.Status.FAIL,
                            screenshotName + " " + twelveHour,
                            com.aventstack.extentreports.MediaEntityBuilder
                                    .createScreenCaptureFromBase64String(base64).build());
                    ExtentCucumberAdapter.addTestStepLog("Screenshot is attached");
                }

                byte[] screenshotBytes = ScreenshotUtils.takeScreenshotAsBytes(driver);
                if (screenshotBytes.length > 0) {
                    scenario.attach(screenshotBytes, "image/png", screenshotName);
                }

                captureScreenshotAsFile(screenshotName, driver);

            } catch (Throwable e) {
                logger.error("Failed to capture screenshot on failure.", e);
            }
        }
        logger.info("In After(order=1) - Scenario: {} | Status: {}", scenario.getName(), scenario.getStatus());
    }

    @After(order = 0)
    public void quitBrowser() {
        DriverFactory.getInstance().closeDriver();
        logger.info("In After(order=0) - Browser closed.");
    }

    private void dismissPopupIfPresent(WebDriver driver) {
        try {
            driver.findElement(org.openqa.selenium.By.tagName("body"))
                  .sendKeys(org.openqa.selenium.Keys.ESCAPE);
            com.walmart.framework.utils.HumanUtils.shortDelay();
            logger.info("Sent ESC key to dismiss any modal overlay.");
        } catch (Exception ignored) {}

        org.openqa.selenium.By[] closeLocators = {
            org.openqa.selenium.By.cssSelector("button[aria-label='Close']"),
            org.openqa.selenium.By.cssSelector("button[aria-label='close']"),
            org.openqa.selenium.By.cssSelector("[class*='modal'] button[class*='close']"),
            org.openqa.selenium.By.cssSelector("[class*='Modal'] button[class*='close']"),
            org.openqa.selenium.By.xpath("//div[contains(@class,'modal') or contains(@class,'Modal')]//button[@aria-label='Close' or @aria-label='close']"),
            org.openqa.selenium.By.cssSelector("[data-automation-id='modal-close-btn']"),
            org.openqa.selenium.By.cssSelector("button[data-testid='modal-close']"),
            org.openqa.selenium.By.cssSelector("[class*='CloseButton']"),
            org.openqa.selenium.By.cssSelector("[class*='close-button']"),
            org.openqa.selenium.By.xpath("//button[contains(normalize-space(),'Accept')]"),
            org.openqa.selenium.By.xpath("//button[contains(normalize-space(),'Got it')]"),
            org.openqa.selenium.By.xpath("//button[contains(normalize-space(),'No thanks')]")
        };

        for (org.openqa.selenium.By locator : closeLocators) {
            try {
                java.util.List<org.openqa.selenium.WebElement> els = driver.findElements(locator);
                if (!els.isEmpty() && els.get(0).isDisplayed()) {
                    els.get(0).click();
                    logger.info("Dismissed popup using: {}", locator);
                    com.walmart.framework.utils.HumanUtils.shortDelay();
                    return;
                }
            } catch (Exception ignored) {}
        }

        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "var patterns = ['ModalPortal_container','ModalPortal_scrim'," +
                "  'OverlayScrim_scrim','BottomSheet_container','Overlay_overlay'];" +
                "patterns.forEach(function(p) {" +
                "  document.querySelectorAll('[class*=\"'+p+'\"]').forEach(function(el){el.remove();});" +
                "});" +
                "document.body.style.overflow='auto';"
            );
            logger.info("Removed ModalPortal elements from DOM.");
        } catch (Exception ignored) {}
    }

    public String captureScreenshotAsFile(String testMethodName, WebDriver driver) {
        org.openqa.selenium.OutputType<File> outputType = org.openqa.selenium.OutputType.FILE;
        File srcFile = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(outputType);
        String captureScreenshotPath = System.getProperty("user.dir")
                + File.separator + "target" + File.separator + "screenshots"
                + File.separator + year + File.separator + monthYear
                + File.separator + dateMonthYear + File.separator
                + twentyFourHour + testMethodName + ".png";
        try {
            FileUtils.copyFile(srcFile, new File(captureScreenshotPath));
        } catch (IOException e) {
            logger.error("Failed to save screenshot file.", e);
        }
        return captureScreenshotPath;
    }
}
