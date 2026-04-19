package com.walmart.framework.pages;

import com.walmart.framework.base.BasePage;
import com.walmart.framework.utils.HumanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * DepartmentsPage — Page Object for the /departments-list page.
 *
 * After clicking "Departments" in the nav, Walmart navigates to
 * https://www.walmart.com/departments-list which shows a flat list
 * of department headings. There is NO flyout/hover — each heading
 * is a clickable link to that department's category page.
 *
 * @author Maniraj
 */
public class DepartmentsPage extends BasePage {

    public static final Logger logger = LogManager.getLogger(DepartmentsPage.class);

    // ─── Toys & Outdoor Play link on /departments-list ────────────────────────
    // The page renders department names as <a> or <h2> elements.
    // We match by text content since href patterns vary.
    private static final By[] TOYS_LINK_LOCATORS = {
        By.xpath("//a[contains(normalize-space(),'Toys') and contains(normalize-space(),'Outdoor')]"),
        By.xpath("//h2[contains(normalize-space(),'Toys')]/ancestor::a"),
        By.xpath("//h2[contains(normalize-space(),'Toys')]/parent::a"),
        By.cssSelector("a[href*='toys_and_outdoor'], a[href*='toys-outdoor']"),
        By.xpath("//*[contains(normalize-space(text()),'Toys & Outdoor Play')]")
    };

    // ─── "All Toys & Outdoor Play" on the Toys category page ─────────────────
    private static final By[] ALL_TOYS_LOCATORS = {
        By.xpath("//a[contains(normalize-space(),'All Toys')]"),
        By.cssSelector("a[href*='kp=toys_and_outdoor_play']"),
        By.cssSelector("a[href*='toys'][href*='all']"),
        By.xpath("//a[normalize-space()='All Toys & Outdoor Play']")
    };

    public DepartmentsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Clicks the "Toys & Outdoor Play" department link on the departments list page.
     * Uses JavaScript to find the link by text — more reliable than CSS/XPath
     * on Walmart's dynamically rendered department list.
     */
    public void clickToysAndOutdoorPlay() {
        logger.info("Clicking Toys & Outdoor Play on departments list page");
        HumanUtils.waitForPageLoadAndPause(driver);

        // JS approach: find any anchor whose text contains 'Toys' and 'Outdoor'
        Boolean clicked = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var links = document.querySelectorAll('a');" +
            "for (var i = 0; i < links.length; i++) {" +
            "  var text = links[i].textContent || links[i].innerText;" +
            "  if (text.indexOf('Toys') !== -1 && text.indexOf('Outdoor') !== -1) {" +
            "    links[i].click();" +
            "    return true;" +
            "  }" +
            "}" +
            "return false;"
        );

        if (Boolean.TRUE.equals(clicked)) {
            logger.info("Clicked Toys & Outdoor Play via JS text search.");
            HumanUtils.waitForPageLoadAndPause(driver);
        } else {
            // Fallback: navigate directly to the Toys category URL
            logger.warn("Toys link not found via JS — navigating directly to Toys URL.");
            ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("window.location.href='https://www.walmart.com/cp/toys/4171';");
            HumanUtils.waitForPageLoadAndPause(driver);
        }
    }

    /**
     * Clicks "All Toys & Outdoor Play" on the Toys category landing page.
     * Uses JS text search + direct URL fallback.
     */
    public void clickAllToys() {
        logger.info("Clicking All Toys & Outdoor Play");
        HumanUtils.waitForPageLoadAndPause(driver);

        Boolean clicked = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "var links = document.querySelectorAll('a');" +
            "for (var i = 0; i < links.length; i++) {" +
            "  var text = links[i].textContent || links[i].innerText;" +
            "  if (text.indexOf('All Toys') !== -1) {" +
            "    links[i].click();" +
            "    return true;" +
            "  }" +
            "}" +
            "return false;"
        );

        if (Boolean.TRUE.equals(clicked)) {
            logger.info("Clicked All Toys via JS text search.");
            HumanUtils.waitForPageLoadAndPause(driver);
        } else {
            // Fallback: navigate directly to All Toys search URL
            logger.warn("All Toys link not found — navigating directly.");
            ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("window.location.href='https://www.walmart.com/browse/toys/4171_4172';");
            HumanUtils.waitForPageLoadAndPause(driver);
        }
    }

    /**
     * Legacy hover method — kept for compatibility but delegates to click.
     * On the current Walmart UI, hover is not needed; clicking is sufficient.
     */
    public void hoverOnToys() {
        logger.info("hoverOnToys() → delegating to clickToysAndOutdoorPlay()");
        clickToysAndOutdoorPlay();
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private WebElement findFirstPresent(By[] locators, String name) {
        for (By locator : locators) {
            try {
                List<WebElement> found = driver.findElements(locator);
                if (!found.isEmpty() && found.get(0).isDisplayed()) {
                    logger.debug("Found '{}' using: {}", name, locator);
                    return found.get(0);
                }
            } catch (Exception ignored) {}
        }
        logger.warn("No immediate match for '{}', waiting on primary locator.", name);
        return waitUtils.waitForElementVisible(locators[0]);
    }

    private void jsClickElement(WebElement el) {
        ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", el);
        HumanUtils.shortDelay();
        ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("arguments[0].click();", el);
    }
}
