package com.walmart.tests.stepdefinitions;

import com.aventstack.extentreports.Status;
import com.walmart.framework.browser.DriverFactory;
import com.walmart.framework.pages.HomePage;
import com.walmart.framework.pages.SearchResultsPage;
import com.walmart.framework.utils.ReportLogs;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * SearchSteps — Step definitions for Search.feature.
 *
 * @author Maniraj
 */
public class SearchSteps {

    public static final Logger logger = LogManager.getLogger(SearchSteps.class);

    @When("I type {string} in the search bar")
    public void iTypeInTheSearchBar(String keyword) {
        logger.info("Step: I type '{}' in the search bar", keyword);
        HomePage homePage = new HomePage(DriverFactory.getInstance().getDriver());
        homePage.searchFor(keyword);
        ReportLogs.addLog(Status.INFO, "Typed '" + keyword + "' in search bar");
    }

    @And("I click on the first search suggestion")
    public void iClickOnTheFirstSearchSuggestion() {
        logger.info("Step: I click on the first search suggestion");
        com.walmart.framework.utils.HumanUtils.mediumDelay();

        String currentUrl = DriverFactory.getInstance().getDriver().getCurrentUrl();
        if (currentUrl.contains("/search") || currentUrl.contains("query=") || currentUrl.contains("q=")) {
            logger.info("Already on search results page — skipping suggestion click.");
            ReportLogs.addLog(Status.INFO, "Search submitted — already on results page");
            return;
        }

        SearchResultsPage searchResultsPage = new SearchResultsPage(DriverFactory.getInstance().getDriver());
        try {
            searchResultsPage.clickFirstSuggestion();
            ReportLogs.addLog(Status.INFO, "Clicked first autocomplete suggestion");
        } catch (Exception e) {
            logger.warn("No suggestion found — waiting for results page to load.");
            com.walmart.framework.utils.HumanUtils.longDelay();
            ReportLogs.addLog(Status.INFO, "Waiting for search results to load");
        }
    }

    @Then("the search results page should be displayed")
    public void theSearchResultsPageShouldBeDisplayed() {
        logger.info("Step: Validating search results page is displayed");
        SearchResultsPage searchResultsPage = new SearchResultsPage(DriverFactory.getInstance().getDriver());

        Assert.assertTrue(
            searchResultsPage.isResultsPageDisplayed(),
            "Expected to be on the search results page but current URL is: "
                + DriverFactory.getInstance().getDriver().getCurrentUrl()
        );

        ReportLogs.addLogWithScreenshot(Status.PASS, "Search results page is displayed");
        logger.info("Search results page confirmed. URL: {}",
                DriverFactory.getInstance().getDriver().getCurrentUrl());
    }

    @And("the search results should be relevant to {string}")
    public void theSearchResultsShouldBeRelevantTo(String keyword) {
        logger.info("Step: Validating results are relevant to '{}'", keyword);
        SearchResultsPage searchResultsPage = new SearchResultsPage(DriverFactory.getInstance().getDriver());

        boolean relevant = searchResultsPage.areResultsRelevantTo(keyword);

        ReportLogs.addLogForStringComparison(
            relevant ? keyword : "no match",
            keyword,
            "Search relevance for '" + keyword + "'"
        );

        Assert.assertTrue(
            relevant,
            "Expected at least 3 product results to contain '" + keyword
                + "' but results appear unrelated. First product: "
                + searchResultsPage.getFirstProductTitle()
        );

        logger.info("Relevance validation passed for keyword: {}", keyword);
    }
}
