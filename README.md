# Walmart BDD Automation Framework

**Author: Maniraj**

A hybrid test automation framework for [walmart.com](https://www.walmart.com) built with Java 17, Selenium 4, Cucumber 7, and TestNG, following the Page Object Model (POM) and BDD principles.

---

## Tech Stack

| Component          | Technology                  | Version  |
|--------------------|-----------------------------|----------|
| Language           | Java                        | 17       |
| Build Tool         | Maven                       | 3.9+     |
| Test Framework     | TestNG                      | 7.9.0    |
| BDD                | Cucumber                    | 7.15.0   |
| Browser Automation | Selenium                    | 4.18.1   |
| Driver Management  | WebDriverManager            | 5.7.0    |
| Reporting          | ExtentReports + Cucumber HTML | 5.1.1  |
| Logging            | Log4j2                      | 2.23.1   |
| Excel Data         | Apache POI                  | 5.2.5    |
| JSON Data          | Gson                        | 2.10.1   |

---

## Prerequisites

- Java 17 (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Google Chrome (latest)
- Microsoft Edge (optional, for cross-browser)

---

## Project Structure

```
src/
├── main/java/com/walmart/framework/
│   ├── base/          BasePage.java
│   ├── browser/       IBrowser.java, BrowserFactory.java, DriverFactory.java
│   ├── config/        ConfigReader.java
│   ├── pages/         HomePage, SearchResultsPage, DepartmentsPage,
│   │                  ProductListingPage, ProductDetailsPage, CartPage
│   └── utils/         WaitUtils, ScreenshotUtils, HumanUtils,
│                      ExcelReader, JsonReader, LoggerUtils, ReportLogs
└── test/
    ├── java/com/walmart/tests/
    │   ├── hooks/         ApplicationHooks.java
    │   ├── runner/        TestRunner.java
    │   └── stepdefinitions/  CommonSteps, SearchSteps, CartSteps
    └── resources/
        ├── config/        config.properties
        ├── features/      Search.feature, Cart.feature
        └── testdata/      testdata.xlsx, testdata.json
```

---

## Configuration

`src/test/resources/config/config.properties`:

```properties
browser=chrome
base.url=https://www.walmart.com
implicit.wait=10
explicit.wait=20
headless=false
```

---

## How to Run

```bash
# Full regression suite
mvn clean test

# Smoke tests only
mvn clean test -Dcucumber.filter.tags="@smoke"

# Run with Edge browser
mvn clean test -Dbrowser=edge

# Headless mode
mvn clean test -Dheadless=true
```

From IDE: open `TestRunner.java` → right-click → Run.

---

## Reports

| Type              | Location                              |
|-------------------|---------------------------------------|
| Cucumber HTML     | `target/cucumber-reports/report.html` |
| ExtentReports     | `target/reports/SparkReport.html`     |
| Surefire XML      | `target/surefire-reports/`            |
| Screenshots       | `target/screenshots/`                 |
| Execution Log     | `target/logs/test-execution.log`      |

---

## Test Scenarios

| Feature | Tag        | Description                                      |
|---------|------------|--------------------------------------------------|
| Search  | @smoke     | Search for iPhone and validate relevant results  |
| Search  | @regression | Data-driven search: Samsung, MacBook, Headphones |
| Cart    | @smoke     | Navigate to Toys, add product, validate cart     |
