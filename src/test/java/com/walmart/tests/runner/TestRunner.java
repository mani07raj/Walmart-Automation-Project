package com.walmart.tests.runner;

import com.aventstack.extentreports.service.ExtentService;
import com.walmart.tests.hooks.ApplicationHooks;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

/**
 * TestRunner — Cucumber TestNG runner.
 *
 * Run via Maven : mvn clean test
 * Tag filter    : mvn clean test -Dcucumber.filter.tags="@smoke"
 * Browser switch: mvn clean test -Dbrowser=edge
 *
 * @author Maniraj
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
            "com.walmart.tests.stepdefinitions",
            "com.walmart.tests.hooks"
        },
        dryRun = false,
        tags = "@regression",
        monochrome = true,
        plugin = {
            "pretty",
            "html:target/cucumber-reports/report.html",
            "json:target/cucumber-reports/report.json",
            "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
            "rerun:target/failedrerun.txt"
        }
)
public class TestRunner extends AbstractTestNGCucumberTests {

    public static final Logger logger = LogManager.getLogger(TestRunner.class);

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        ExtentService.getInstance().setSystemInfo("Application", "Walmart");
        ExtentService.getInstance().setSystemInfo("User Name", System.getProperty("user.name"));
        ExtentService.getInstance().setSystemInfo("Environment", "QA");
        ExtentService.getInstance().setSystemInfo("OS", System.getProperty("os.name"));
        ExtentService.getInstance().setSystemInfo("OS Version", System.getProperty("os.version"));
        ExtentService.getInstance().setSystemInfo("OS Arch", System.getProperty("os.arch"));
        logger.info("TestRunner @BeforeClass — Extent system info populated.");
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        logger.info("TestRunner @AfterClass");
        try {
            if (ApplicationHooks.driver != null) {
                ExtentService.getInstance().setSystemInfo("Browser", getBrowser());
                ExtentService.getInstance().setSystemInfo("Browser Version", getBrowserVersion());
            }
        } catch (Exception e) {
            logger.warn("Could not retrieve browser info for Extent report: {}", e.getMessage());
        }
    }

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    private String getBrowser() {
        Capabilities cap = ((RemoteWebDriver) ApplicationHooks.driver).getCapabilities();
        return cap.getBrowserName();
    }

    private String getBrowserVersion() {
        Capabilities cap = ((RemoteWebDriver) ApplicationHooks.driver).getCapabilities();
        return cap.getBrowserVersion();
    }
}
