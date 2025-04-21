package com.AutoPOC;

import com.AutoPOC.pages.AddProductsToCartAndPlaceOrder;
import com.AutoPOC.pages.LoginPage;
import com.AutoPOC.pages.OrderInformationPage;
import com.AutoPOC.utils.*;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Base test class for initializing the browser, logging in, and recording results.
 * Provides shared setup/teardown and handles test data loading.
 */
public class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    private static Instant startTime;

    protected WebDriver driver;
    protected LoginPage loginPage;
    protected AddProductsToCartAndPlaceOrder addProductsToCartAndPlaceOrder;
    protected OrderInformationPage orderInformationPage;

    @BeforeSuite
    public void suiteSetup() {
        startTime = Instant.now();
        logger.info("Test Execution Started at: {}", getCurrentTime());
    }

    @BeforeMethod
    public void setUp() {
        logger.info("Setting up WebDriver before test execution.");
    }

    /**
     * Core setup routine that runs once per test case (TestID).
     * Initializes browser, navigates to the URL, and logs in with test data.
     */
    @Test(dataProvider = "testData")
    public void executeTestForTestID(String testID, ITestContext context) {
        logger.info("Fetching test data for TestID: {}", testID);

        Map<String, String> testData = TestDataUtil.getTestCaseByTestID(testID);
        if (testData == null) {
            throw new RuntimeException("TestID " + testID + " not found in Excel!");
        }

        String browser = testData.getOrDefault(TestDataKeys.BROWSER, "chrome");
        String testURL = testData.getOrDefault(TestDataKeys.URL, "about:blank");
        String username = testData.get(TestDataKeys.USERNAME);
        String password = testData.get(TestDataKeys.PASSWORD);

        logger.info("Running TestID={} on browser={}", testID, browser);
        logger.info("URL: {}", testURL);

        context.setAttribute("TestID", testID);
        context.setAttribute("Browser", browser);

        try {
            DriverFactory.initializeDriver(browser);
            driver = DriverFactory.getDriver();
            driver.get(testURL);
            logger.info("Navigated to: {}", testURL);
        } catch (Exception e) {
            logger.error("WebDriver init failed", e);
            throw new RuntimeException(e);
        }

        loginPage = new LoginPage();
        addProductsToCartAndPlaceOrder = new AddProductsToCartAndPlaceOrder();
        orderInformationPage = new OrderInformationPage();

        try {
            loginPage.login(username, password);
            logger.info("Logged in as {}", username);
        } catch (Exception e) {
            logger.error("Login failed for {}", username, e);
            throw new RuntimeException(e);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void recordExecutionData(ITestResult result) {
        if (orderInformationPage != null) {
            try {
                Object attr = result.getTestContext().getAttribute("ExcelRowIndex");
                if (attr instanceof Integer rowIndex) {
                    ExecutionDataUtil.writeExecutionData(rowIndex, result);
                    logger.info("Execution data recorded at row {}", rowIndex + 1);
                } else {
                    logger.warn("ExcelRowIndex not found in context. Skipping writeExecutionData.");
                }
            } catch (Exception e) {
                logger.error("Failed to write execution data", e);
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        try {
            DriverFactory.quitDriver();
        } catch (Exception e) {
            logger.error("Error quitting WebDriver", e);
        }
    }

    @AfterMethod
    public void clearContext() {
        TestContextManager.clear();
    }

    @AfterSuite
    public void suiteTearDown() {
        logger.info("Test Execution Ended at: {}", getCurrentTime());
        logger.info("Total Execution Time: {}", getExecutionDuration());
    }

    @DataProvider(name = "testData")
    public Object[][] getTestData() {
        return TestDataUtil.getAllTestIDs();
    }

    @DataProvider(name = "syntheticData")
    public Object[][] syntheticData() {
        return SyntheticDataUtil.getAllInputIDs();
    }

    // ─── Utility Methods ────────────────────────────────────────────────

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String getExecutionDuration() {
        Duration d = Duration.between(startTime, Instant.now());
        return String.format("%02d min, %02d sec", d.toMinutes(), d.getSeconds() % 60);
    }
}
