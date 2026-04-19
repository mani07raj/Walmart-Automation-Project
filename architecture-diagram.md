# Walmart BDD Automation Framework — Architecture

**Author: Maniraj**

---

## Execution Flow

```
mvn clean test  /  IDE Run  /  -Dcucumber.filter.tags="@smoke"
        │
        ▼
  testng.xml  ──►  TestRunner.java (@CucumberOptions)
                        │
                        ▼
              Cucumber Engine
              ┌─────────────────┐   ┌─────────────────┐
              │ Search.feature  │   │  Cart.feature   │
              │ @regression     │   │  @regression    │
              │ @search @smoke  │   │  @cart @smoke   │
              └────────┬────────┘   └────────┬────────┘
                       └──────────┬──────────┘
                                  ▼
                     ApplicationHooks.java
                     ┌──────────────────────────────────┐
                     │ @Before(0) → ConfigReader         │
                     │ @Before(1) → BrowserFactory       │
                     │              DriverFactory        │
                     │              driver.get(baseUrl)  │
                     │ @After(1)  → Screenshot on fail   │
                     │ @After(0)  → driver.quit()        │
                     └──────────────────────────────────┘
                                  │
                                  ▼
                     Step Definitions (glue)
              ┌──────────────┬──────────────┬──────────────┐
              │ CommonSteps  │ SearchSteps  │  CartSteps   │
              └──────┬───────┴──────┬───────┴──────┬───────┘
                     │              │              │
                     └──────────────┴──────────────┘
                                  │
                     DriverFactory.getInstance().getDriver()
                                  │
                                  ▼
                     Page Object Model (POM)
        ┌──────────┬──────────────────┬─────────────────┐
        │ HomePage │ SearchResultsPage│ DepartmentsPage │
        └──────────┴──────────────────┴─────────────────┘
        ┌──────────────────────┬──────────────────┬──────────┐
        │ ProductListingPage   │ ProductDetailsPage│ CartPage │
        └──────────────────────┴──────────────────┴──────────┘
                     All extend BasePage
                                  │
                                  ▼
                     BasePage (abstract)
                     click() / sendKeys() / getText()
                     hoverOver() / jsClick() / isDisplayed()
                                  │
                                  ▼
                     Utility Layer
        ┌──────────────┬──────────────────┬────────────┬──────────────┐
        │  WaitUtils   │ ScreenshotUtils  │ ExcelReader│  JsonReader  │
        │  HumanUtils  │  LoggerUtils     │ ReportLogs │              │
        └──────────────┴──────────────────┴────────────┴──────────────┘
                                  │
                                  ▼
                     Browser / Driver Layer
        ┌──────────────────────────────────────────────────────┐
        │  IBrowser (interface)                                │
        │  BrowserFactory implements IBrowser                  │
        │    chrome  → ChromeDriver  (WebDriverManager)        │
        │    edge    → EdgeDriver    (WebDriverManager)        │
        │    firefox → FirefoxDriver (WebDriverManager)        │
        │  DriverFactory (Singleton + ThreadLocal<WebDriver>)  │
        └──────────────────────────────────────────────────────┘
                                  │
                                  ▼
                     https://www.walmart.com
```

---

## Configuration

```
ConfigReader  ──►  config/config.properties
                   browser, base.url, timeouts, headless
              ──►  System.getProperty() overrides (-Dbrowser, -Dheadless)
```

---

## Reporting

```
Cucumber HTML   →  target/cucumber-reports/report.html
Cucumber JSON   →  target/cucumber-reports/report.json
ExtentReports   →  target/reports/SparkReport.html
Surefire XML    →  target/surefire-reports/
Screenshots     →  target/screenshots/
Execution Log   →  target/logs/test-execution.log
```

---

## OOP Design

| Concept       | Implementation                                              |
|---------------|-------------------------------------------------------------|
| Encapsulation | All `By` locators in page classes are `private static final`|
| Abstraction   | `BasePage` is abstract; `BrowserFactory` hides WebDriverManager |
| Inheritance   | All page classes extend `BasePage`                          |
| Polymorphism  | `BrowserFactory` returns Chrome, Edge, or Firefox at runtime|
| Interface     | `IBrowser` defines the driver contract                      |
| Singleton     | `DriverFactory.getInstance()`                               |
| Factory       | `BrowserFactory.createBrowserInstance()`                    |
| Builder       | `ExcelReader.Builder`                                       |
