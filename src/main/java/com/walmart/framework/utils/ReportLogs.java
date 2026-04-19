package com.walmart.framework.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.walmart.framework.browser.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/**
 * ReportLogs — Extent report logging utility with Log4j2 fallback.
 * All methods guard against a null ExtentTest step, which occurs when tests
 * are executed directly from the IDE without the Extent adapter plugin active.
 *
 * @author Maniraj
 */
public class ReportLogs {

    public static final Logger logger = LogManager.getLogger(ReportLogs.class);

    public static void addLog(Status status, String message) {
        logger.info("Log Message: {} - {}", status, message);
        ExtentTest step = getCurrentStep();
        if (step == null) return;
        step.log(status, MarkupHelper.createLabel("Log Message: " + status + " - " + message, ExtentColor.TEAL));
    }

    public static void addLogWithMarkUp(Status status, String message) {
        logger.info("Log Message: {} - {}", status, message);
        ExtentTest step = getCurrentStep();
        if (step == null) return;
        step.log(status, MarkupHelper.createLabel("Log Message: " + status + " - " + message, getColorForStatus(status)));
    }

    public static void addLogWithError(Status status, Throwable throwable) {
        logger.info("Log Message: {} : ", status, throwable);
        ExtentTest step = getCurrentStep();
        if (step == null) return;
        step.log(status, throwable);
    }

    public static void addLogWithScreenshot(Status status, String message) {
        logger.info("Log Message: {} - {}", status, message);
        ExtentTest step = getCurrentStep();
        if (step == null) return;
        try {
            String screenshot = getBase64Image();
            step.log(status,
                    "Log Message: " + status + " - " + message,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot).build());
            ExtentCucumberAdapter.addTestStepLog("Screenshot is attached");
        } catch (WebDriverException e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage(), e);
        }
    }

    public static void addLogWithErrorAndScreenshot(Status status, Throwable throwable) {
        logger.info("Log Message: {} : ", status, throwable);
        ExtentTest step = getCurrentStep();
        if (step == null) return;
        try {
            String screenshot = getBase64Image();
            step.log(status, throwable,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot).build());
            ExtentCucumberAdapter.addTestStepLog("Screenshot is attached");
        } catch (WebDriverException e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage(), e);
        }
    }

    public static void addLogForStringComparison(String actual, String expected, String message) {
        if (actual.contains(expected)) {
            addLogWithMarkUp(Status.PASS,
                    message + ": actual - <b><i>" + actual + "</i></b> & expected - <b><i>" + expected + "</i></b>");
        } else {
            addLogWithMarkUp(Status.FAIL,
                    message + ": actual - <b><i>" + actual + "</i></b> & expected - <b><i>" + expected + "</i></b>");
        }
    }

    private static ExtentTest getCurrentStep() {
        try {
            return ExtentCucumberAdapter.getCurrentStep();
        } catch (Exception e) {
            return null;
        }
    }

    private static String getBase64Image() throws WebDriverException {
        WebDriver driver = DriverFactory.getInstance().getDriver();
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }

    private static ExtentColor getColorForStatus(Status status) {
        return switch (status) {
            case PASS    -> ExtentColor.GREEN;
            case FAIL    -> ExtentColor.RED;
            case INFO    -> ExtentColor.BLUE;
            case WARNING -> ExtentColor.YELLOW;
            case SKIP    -> ExtentColor.ORANGE;
            default      -> ExtentColor.GREY;
        };
    }
}
