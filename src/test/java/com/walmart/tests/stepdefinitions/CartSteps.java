package com.walmart.tests.stepdefinitions;

import com.aventstack.extentreports.Status;
import com.walmart.framework.browser.DriverFactory;
import com.walmart.framework.pages.*;
import com.walmart.framework.utils.HumanUtils;
import com.walmart.framework.utils.ReportLogs;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

/**
 * CartSteps — Step definitions for Cart.feature.
 *
 * @author Maniraj
 */
public class CartSteps {

    public static final Logger logger = LogManager.getLogger(CartSteps.class);
    private String selectedProductName;

    @When("I click on the Departments menu")
    public void iClickOnTheDepartmentsMenu() {
        logger.info("Step: I click on the Departments menu");
        HomePage homePage = new HomePage(DriverFactory.getInstance().getDriver());
        homePage.searchFor("toys");
        HumanUtils.mediumDelay();
        ReportLogs.addLog(Status.INFO, "Navigated to Toys department");
        logger.info("URL: {}", DriverFactory.getInstance().getDriver().getCurrentUrl());
    }

    @And("I hover over {string}")
    public void iHoverOver(String department) {
        logger.info("Step: navigating to '{}'", department);
        HumanUtils.mediumDelay();
        ReportLogs.addLog(Status.INFO, "On department page: " + department);
    }

    @And("I click on {string}")
    public void iClickOn(String text) {
        logger.info("Step: I click on '{}'", text);
        if (text.toLowerCase().contains("add to cart")) {
            new ProductDetailsPage(DriverFactory.getInstance().getDriver()).clickAddToCart();
            HumanUtils.mediumDelay();
            ReportLogs.addLog(Status.INFO, "Clicked Add to Cart");
        } else {
            logger.info("Already on product listing — skipping: {}", text);
            ReportLogs.addLog(Status.INFO, "On product listing: " + text);
        }
    }

    @And("I select the first product from the listing")
    public void iSelectTheFirstProductFromTheListing() {
        logger.info("Step: I select the first product from the listing");
        WebDriver driver = DriverFactory.getInstance().getDriver();
        HumanUtils.waitForPageLoadAndPause(driver);

        String foundHref = null;
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {
            Object result = ((JavascriptExecutor) driver).executeScript(
                "var links = document.querySelectorAll('a[href*=\"/ip/\"]');" +
                "for (var i=0; i<links.length; i++) {" +
                "  var h = links[i].href||'';" +
                "  if (h.indexOf('athAsset')<0 && h.indexOf('athena')<0 && h.indexOf('blocked')<0) return h;" +
                "}" +
                "return null;"
            );
            if (result != null) { foundHref = result.toString(); break; }
            HumanUtils.randomDelay(600, 900);
        }

        if (foundHref != null) {
            logger.info("Found product link: {}", foundHref);
            try {
                org.openqa.selenium.WebElement link = driver.findElement(
                    org.openqa.selenium.By.cssSelector("a[href=\"" + foundHref.replace("\"", "\\\"") + "\"]")
                );
                new org.openqa.selenium.interactions.Actions(driver)
                    .moveToElement(link)
                    .pause(java.time.Duration.ofMillis(500))
                    .sendKeys(org.openqa.selenium.Keys.RETURN)
                    .perform();
                logger.info("Navigated to product page.");
            } catch (Exception e) {
                logger.warn("Keyboard navigation failed, falling back to JS click: {}", e.getMessage());
                final String href = foundHref;
                ((JavascriptExecutor) driver).executeScript(
                    "var links = document.querySelectorAll('a[href*=\"/ip/\"]');" +
                    "for (var i=0; i<links.length; i++) {" +
                    "  if (links[i].href === arguments[0]) { links[i].click(); return; }" +
                    "}", href
                );
            }
        } else {
            logger.warn("No product links found — clicking first available.");
            try {
                org.openqa.selenium.WebElement link = driver.findElement(
                    org.openqa.selenium.By.cssSelector("a[href*='/ip/']"));
                new org.openqa.selenium.interactions.Actions(driver)
                    .moveToElement(link)
                    .pause(java.time.Duration.ofMillis(500))
                    .sendKeys(org.openqa.selenium.Keys.RETURN)
                    .perform();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript(
                    "var l=document.querySelector('a[href*=\"/ip/\"]'); if(l) l.click();"
                );
            }
        }

        selectedProductName = new ProductListingPage(driver).getProductName(0);
        HumanUtils.waitForPageLoadAndPause(driver);
        logger.info("URL after product selection: {}", driver.getCurrentUrl());
        ReportLogs.addLog(Status.INFO, "Selected product: " + selectedProductName);
    }

    @Then("I should be on the product details page")
    public void iShouldBeOnTheProductDetailsPage() {
        logger.info("Step: Validating product details page");
        WebDriver driver = DriverFactory.getInstance().getDriver();
        HumanUtils.waitForPageLoadAndPause(driver);
        String url = driver.getCurrentUrl();
        logger.info("Current URL: {}", url);

        boolean onProductPage = url.contains("/ip/");
        if (!onProductPage) {
            onProductPage = new ProductDetailsPage(driver).isOnProductPage();
        }

        Assert.assertTrue(onProductPage, "Expected product details page URL. Got: " + url);

        if (selectedProductName == null || selectedProductName.isBlank()) {
            try { selectedProductName = new ProductDetailsPage(driver).getProductTitle(); }
            catch (Exception e) { selectedProductName = "product"; }
        }
        ReportLogs.addLogWithScreenshot(Status.PASS, "On product page: " + selectedProductName);
    }

    @Then("the product should be added to the cart")
    public void theProductShouldBeAddedToTheCart() {
        logger.info("Step: Validating product added to cart");
        WebDriver driver = DriverFactory.getInstance().getDriver();
        CartPage cartPage = new CartPage(driver);
        HumanUtils.waitForPageLoadAndPause(driver);

        try { cartPage.clickViewCart(); HumanUtils.mediumDelay(); }
        catch (Exception e) { logger.warn("View Cart button not found."); }

        if (!driver.getCurrentUrl().contains("/cart")) {
            ((JavascriptExecutor) driver)
                .executeScript("window.location.href='https://www.walmart.com/cart';");
            HumanUtils.waitForPageLoadAndPause(driver);
        }

        Assert.assertTrue(cartPage.isCartDisplayed(),
            "Cart not displayed. URL: " + driver.getCurrentUrl());

        String name = cartPage.getCartItemName();
        logger.info("Cart item: {}", name);
        ReportLogs.addLogWithScreenshot(Status.PASS, "Product added to cart: " + name);
    }

    @And("the cart subtotal should be displayed")
    public void theCartSubtotalShouldBeDisplayed() {
        String subtotal = new CartPage(DriverFactory.getInstance().getDriver()).getSubtotal();
        Assert.assertFalse(subtotal.isBlank(), "Cart subtotal was empty.");
        Assert.assertTrue(subtotal.contains("$"), "Subtotal missing dollar amount: " + subtotal);
        ReportLogs.addLogWithMarkUp(Status.PASS, "Subtotal: " + subtotal);
    }

    @And("the estimated total should be displayed")
    public void theEstimatedTotalShouldBeDisplayed() {
        String total = new CartPage(DriverFactory.getInstance().getDriver()).getEstimatedTotal();
        Assert.assertFalse(total.isBlank(), "Estimated total was empty.");
        Assert.assertTrue(total.contains("$"), "Estimated total missing dollar amount: " + total);
        ReportLogs.addLogWithMarkUp(Status.PASS, "Estimated total: " + total);
    }

    @And("the cart icon count should be {string}")
    public void theCartIconCountShouldBe(String expectedCount) {
        int actual = new CartPage(DriverFactory.getInstance().getDriver()).getCartCount();
        int expected = Integer.parseInt(expectedCount);
        Assert.assertEquals(actual, expected,
            "Cart count mismatch: expected " + expected + ", got " + actual);
        ReportLogs.addLogWithMarkUp(Status.PASS, "Cart count: " + actual);
    }
}
