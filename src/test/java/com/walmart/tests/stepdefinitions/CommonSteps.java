package com.walmart.tests.stepdefinitions;

import com.aventstack.extentreports.Status;
import com.walmart.framework.browser.DriverFactory;
import com.walmart.framework.pages.HomePage;
import com.walmart.framework.utils.ReportLogs;
import io.cucumber.java.en.Given;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * CommonSteps — Background step shared across all feature files.
 * Validates the homepage is loaded without triggering a second navigation.
 *
 * @author Maniraj
 */
public class CommonSteps {

    public static final Logger logger = LogManager.getLogger(CommonSteps.class);

    @Given("I am on the Walmart homepage")
    public void iAmOnTheWalmartHomepage() {
        logger.info("Step: I am on the Walmart homepage");

        HomePage homePage = new HomePage(DriverFactory.getInstance().getDriver());

        Assert.assertTrue(
            homePage.isHomePageLoaded(),
            "Expected page title to contain 'Walmart' but was: "
                + DriverFactory.getInstance().getDriver().getTitle()
        );

        ReportLogs.addLogWithScreenshot(Status.INFO, "Confirmed on Walmart homepage");
        logger.info("Walmart homepage confirmed. Title: {}",
                DriverFactory.getInstance().getDriver().getTitle());
    }
}
