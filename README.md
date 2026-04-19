# Walmart BDD Automation Framework

**Site Under Test:** [https://www.walmart.com](https://www.walmart.com)  
**Framework Type:** Hybrid — BDD (Cucumber) + Page Object Model + Data Driven  
**Language:** Java 17  
**Build Tool:** Maven  
**Author:** Maniraj

---

## Table of Contents

1. [Framework Overview](#framework-overview)
2. [Project Structure](#project-structure)
3. [Tech Stack](#tech-stack)
4. [OOP Concepts](#oop-concepts)
5. [Test Scenarios](#test-scenarios)
6. [Prerequisites](#prerequisites)
7. [How to Run](#how-to-run)
8. [Execution Reports](#execution-reports)
9. [Cross Browser Support](#cross-browser-support)
10. [Data Driven Testing](#data-driven-testing)
11. [Architecture Diagram](#architecture-diagram)
12. [Configuration](#configuration)

---

## Framework Overview

This is a BDD automation framework built for Walmart.com. It combines:

- **BDD (Cucumber 7)** — human-readable feature files written in Gherkin
- **Page Object Model** — each page has its own class with private locators
- **Data Driven Testing** — Scenario Outline with Examples table + JSON external data
- **Cross-browser support** — Chrome, Edge, and Firefox via WebDriverManager
- **Realistic interaction timing** — HumanUtils introduces randomised delays and natural mouse movement to handle dynamic page behaviour
- **Overlay handling** — automatic dismissal of modal popups and overlays before interactions

---

## Project Structure

```
WalmartAutomationAssignment/
│
├── src/main/java/com/walmart/framework/
│   ├── base/
│   │   └── BasePage.java              # Abstract base — all pages extend this
│   ├── browser/
│   │   ├── IBrowser.java              # Interface for browser contract
│   │   ├── BrowserFactory.java        # Chrome / Edge / Firefox setup
│   │   └── DriverFactory.java         # Singleton + ThreadLocal WebDriver
│   ├── config/
│   │   └── ConfigReader.java          # Reads config.properties
│   ├── pages/
│   │   ├── HomePage.java
│   │   ├── SearchResultsPage.java
│   │   ├── DepartmentsPage.java
│   │   ├── ProductListingPage.java
│   │   ├── ProductDetailsPage.java
│   │   └── CartPage.java
│   └── utils/
│       ├── WaitUtils.java             # Explicit + Fluent waits
│       ├── HumanUtils.java            # Realistic interaction timing
│       ├── ScreenshotUtils.java       # Screenshot capture (file + base64 + bytes)
│       ├── ReportLogs.java            # Extent report logging utility
│       ├── LoggerUtils.java           # Log4j2 wrapper
│       ├── JsonReader.java            # Gson-based JSON data reader
│       └── ExcelReader.java           # Apache POI Excel reader (Builder pattern)
│
├── src/test/java/com/walmart/tests/
│   ├── hooks/
│   │   └── ApplicationHooks.java      # @Before/@After — browser lifecycle + screenshots
│   ├── runner/
│   │   └── TestRunner.java            # Cucumber TestNG runner + Extent system info
│   └── stepdefinitions/
│       ├── CommonSteps.java           # Shared: Given I am on the Walmart homepage
│       ├── SearchSteps.java           # Search scenario steps
│       └── CartSteps.java             # Cart scenario steps
│
├── src/test/resources/
│   ├── features/
│   │   ├── Search.feature             # Scenario 1 + Scenario Outline
│   │   └── Cart.feature               # Scenario 2 — full cart flow
│   ├── config/
│   │   └── config.properties          # browser, base.url, timeouts
│   ├── testdata/
│   │   ├── testdata.json              # JSON test data for data-driven search
│   │   └── testdata.xlsx              # Excel test data (Apache POI)
│   ├── extent.properties              # ExtentReports configuration
│   ├── extent-config.xml              # Report theme, title, timestamp
│   └── log4j2.xml                     # Log4j2 console + rolling file config
│
├── testng.xml                         # TestNG suite configuration
├── pom.xml                            # Maven dependencies + Surefire plugin
├── architecture-diagram.md            # Full framework architecture
└── README.md                          # This file
```

---

## Tech Stack

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 17 | Programming language |
| Maven | 3.x | Build and dependency management |
| Selenium WebDriver | 4.18.1 | Browser automation |
| Cucumber | 7.15.0 | BDD framework |
| TestNG | 7.9.0 | Test runner + lifecycle |
| WebDriverManager | 5.7.0 | Auto ChromeDriver/EdgeDriver setup |
| ExtentReports | 5.1.1 | Rich HTML execution reports |
| Log4j2 | 2.23.1 | Logging to console and file |
| Apache POI | 5.2.5 | Excel data reading |
| Gson | 2.10.1 | JSON data reading |
| Commons IO | 2.15.1 | File utilities |
| Jackson | 2.17.0 | JSON support for ExtentReports |

---

## OOP Concepts

### Encapsulation
All page locators are `private static final`. Only public business methods are exposed.
```java
// SearchResultsPage.java
private static final By PRODUCT_TITLES =
    By.cssSelector("[data-automation-id='product-title']");

public boolean areResultsRelevantTo(String keyword) { ... }  // only this is visible outside
```

### Abstraction
`BasePage` is `abstract` — hides all Selenium and interaction complexity from step definitions.  
`IBrowser` is an interface — defines the contract for browser creation.
```java
public abstract class BasePage {
    protected void click(By locator) { /* Selenium + HumanUtils hidden here */ }
    protected void sendKeys(By locator, String text) { ... }
}
```

### Inheritance
All 6 page classes extend `BasePage` and inherit `click()`, `sendKeys()`, `getText()`, `jsClick()`, `waitUtils`, and `actions`.
```
BasePage ← HomePage, SearchResultsPage, DepartmentsPage,
           ProductListingPage, ProductDetailsPage, CartPage
```

### Polymorphism
`BrowserFactory` implements `IBrowser` — the same `createBrowserInstance()` method creates different drivers at runtime based on configuration.
```java
bf.createBrowserInstance("chrome");   // → ChromeDriver
bf.createBrowserInstance("edge");     // → EdgeDriver
bf.createBrowserInstance("firefox");  // → FirefoxDriver
```

### Singleton + Factory
`DriverFactory` uses the Singleton pattern to ensure a single instance per lifecycle, with `ThreadLocal<WebDriver>` for thread-safe parallel execution.
```java
DriverFactory.getInstance().setDriver(driver);
DriverFactory.getInstance().getDriver();
```

### Builder Pattern
`ExcelReader` uses a fluent Builder for configuration:
```java
ExcelReader reader = new ExcelReader.Builder()
    .setFileLocation(filePath)
    .setSheet("SearchData")
    .build();
```

---

## Test Scenarios

### Scenario 1 — Search for iPhone (`Search.feature`)
1. Open Walmart homepage
2. Type `"iPhone"` in the search bar
3. Submit search
4. Verify search results page is displayed (`/search?q=iPhone`)
5. Verify results are relevant to the keyword

### Scenario 2 — Add to Cart via Departments (`Cart.feature`)
1. Open Walmart homepage
2. Navigate to Toys department via search
3. Select the first product from the listing
4. Verify product details page is displayed
5. Click **Add to Cart**
6. Navigate to cart
7. Verify product is present in cart
8. Verify subtotal is displayed with a dollar amount
9. Verify estimated total is displayed
10. Verify cart icon count equals 1

### Scenario Outline — Search for Multiple Products (`Search.feature`)
Runs search for 3 additional keywords from the Examples table:
- Samsung
- MacBook
- Headphones

---

## Prerequisites

1. **Java 17** installed — verify: `java -version`
2. **Maven** installed OR use IntelliJ's bundled Maven
3. **Google Chrome** installed (latest)
4. **IntelliJ IDEA** (recommended)
5. Internet connection (tests run on live Walmart.com)

---

## How to Run

### Option 1 — IntelliJ Terminal

```bash
# Using IntelliJ's bundled Maven (Windows)
& "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd" clean test
```

### Option 2 — If Maven is in PATH

```bash
mvn clean test
```

### Option 3 — IntelliJ Maven Panel
Right side **Maven** panel → `Lifecycle` → double-click `clean` then `test`

### Option 4 — Run from IDE
Open `TestRunner.java` → right-click → **Run 'TestRunner'**

### Run Specific Tags

```bash
# Only smoke tests
mvn clean test -Dcucumber.filter.tags="@smoke"

# Only search tests
mvn clean test -Dcucumber.filter.tags="@search"

# Only cart tests
mvn clean test -Dcucumber.filter.tags="@cart"

# Full regression suite
mvn clean test -Dcucumber.filter.tags="@regression"
```

### Run on Different Browser

```bash
# Edge browser
mvn clean test -Dbrowser=edge

# Firefox browser
mvn clean test -Dbrowser=firefox
```

---

## Execution Reports

After running, all reports are generated in the `target/` folder:

### 1. Extent Spark Report (Recommended)
```
target/reports/<year>/<month>/<date>/SparkReport.html
```
- Rich HTML dashboard with pass/fail charts
- Step-by-step logs per scenario
- Screenshots embedded on failure
- System info: Browser, OS, URL, Environment

### 2. Cucumber HTML Report
```
target/cucumber-reports/report.html
```

### 3. Cucumber JSON Report
```
target/cucumber-reports/report.json
```

### 4. Screenshots on Failure
```
target/screenshots/<year>/<month>/<date>/<time>_<ScenarioName>.png
```

### 5. Log File
```
target/logs/test-execution.log
```

### 6. Failed Rerun File
```
target/failedrerun.txt
```

> **To open reports:** Right-click the `.html` file in IntelliJ → Open In → Browser

---

## Cross Browser Support

| Browser | Config Value | Status |
|---------|-------------|--------|
| Chrome | `browser=chrome` | ✅ Default |
| Edge | `browser=edge` | ✅ Supported |
| Firefox | `browser=firefox` | ✅ Supported |

Change browser in `src/test/resources/config/config.properties`:
```properties
browser=chrome
```
Or override at runtime:
```bash
mvn clean test -Dbrowser=edge
```

---

## Data Driven Testing

### 1. Scenario Outline (Feature file Examples table)
```gherkin
Scenario Outline: Search for multiple products
  When I type "<product>" in the search bar
  And I click on the first search suggestion
  Then the search results page should be displayed
  And the search results should be relevant to "<product>"
  Examples:
    | product    |
    | Samsung    |
    | MacBook    |
    | Headphones |
```

### 2. JSON External File
File: `src/test/resources/testdata/testdata.json`
```json
[
  { "keyword": "iPhone",     "expectedMinResults": 10, "category": "Electronics" },
  { "keyword": "Samsung",    "expectedMinResults": 10, "category": "Electronics" },
  { "keyword": "MacBook",    "expectedMinResults": 5,  "category": "Computers"   },
  { "keyword": "Headphones", "expectedMinResults": 10, "category": "Electronics" }
]
```
Read via `JsonReader.readJsonArray(filePath, clazz)` using Gson.

### 3. Excel (ExcelReader utility available)
File: `src/test/resources/testdata/testdata.xlsx`

| keyword    | expectedMinResults | category    |
|------------|--------------------|-------------|
| iPhone     | 10                 | Electronics |
| Samsung    | 10                 | Electronics |
| MacBook    | 5                  | Computers   |
| Headphones | 10                 | Electronics |

Read via `ExcelReader.readData(filePath, sheetName)` using Apache POI.

---

## Architecture Diagram

See [`architecture-diagram.md`](architecture-diagram.md) in the project root for the complete framework architecture including all layers, data flow, and OOP design.

---

## Configuration

`src/test/resources/config/config.properties`:

```properties
# Browser selection: chrome | edge | firefox
browser=chrome

# Application under test
base.url=https://www.walmart.com

# Wait timeouts (seconds)
implicit.wait=10
explicit.wait=20

# Headless mode (true for CI/CD pipelines)
headless=false

# Output directories
screenshot.path=target/screenshots
report.path=target/reports
```

All properties can be overridden at runtime using Maven `-D` flags:
```bash
mvn clean test -Dbrowser=edge -Dheadless=true
```
