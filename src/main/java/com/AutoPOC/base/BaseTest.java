package com.AutoPOC.base;

import com.AutoPOC.config.ConfigReader;
import com.AutoPOC.pages.AddProductsToCartAndPlaceOrder;
import com.AutoPOC.pages.LoginPage;
import com.AutoPOC.pages.OrderInformationPage;
import com.AutoPOC.utils.context.TestContextManager;
import com.AutoPOC.utils.context.TestDataKeys;
import com.AutoPOC.utils.core.DriverFactory;
import com.AutoPOC.utils.core.ScreenshotUtil;
import com.AutoPOC.utils.data.ExecutionDataUtil;
import com.AutoPOC.utils.data.SyntheticDataUtil;
import com.AutoPOC.utils.data.TestDataUtil;
import com.AutoPOC.utils.excel.ExcelColumnIndex;
import com.AutoPOC.utils.excel.ExcelReaderUtil;
import com.AutoPOC.utils.excel.ExcelUtil;
import com.AutoPOC.utils.reporting.ExtentReportManager;
import org.apache.poi.ss.usermodel.Sheet;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
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

        // Initialize ExtentReports
        ExtentReportManager.initReport();
    }

    @BeforeMethod
    public void setUp(Method method) {
        ExtentReportManager.createTest(method.getName());
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
        boolean testPassed = false;

        try {
            // as soon as test finishes (success or fail), figure out the row once
            int rowIndex;
            Object attr = result.getTestContext().getAttribute("ExcelRowIndex");
            if (attr instanceof Integer idx) {
                rowIndex = idx;
            } else {
                // first‐time allocation: pick next free row under column F, row 2+
                Sheet sheet = ExcelReaderUtil.getSheet(
                        ConfigReader.getProperty("Test_Data_File_Path"),
                        ConfigReader.getProperty("Transactional_Data_Sheet_Name")
                );
                rowIndex = ExcelUtil.findNextAvailableRow(sheet, ExcelColumnIndex.RUN_ID, 2);
                result.getTestContext().setAttribute("ExcelRowIndex", rowIndex);
            }

// now ALWAYS write the execution data (including failure reason)
            ExecutionDataUtil.writeExecutionData(rowIndex, result);
        } catch (Exception e) {
            logger.error("Failed to write execution data", e);
        }

        // Logging AFTER writing execution data
        try {
            switch (result.getStatus()) {
                case ITestResult.SUCCESS -> ExtentReportManager.logPass("Test Passed: " + result.getName());
                case ITestResult.FAILURE -> {
                    ExtentReportManager.logFail("Test Failed: " + result.getThrowable());
                    String screenshotPath = ScreenshotUtil.saveScreenshotAsPNG(driver, result.getName());
                    ExtentReportManager.attachScreenshotFromPath(screenshotPath, "Failure Screenshot");
                    logger.error("Failure details:", result.getThrowable());
                }
                case ITestResult.SKIP -> ExtentReportManager.logSkip("Test Skipped: " + result.getName());
            }
        } catch (Exception e) {
            logger.error("Error while logging to ExtentReport", e);
        }

        // Remove test from Extent context
        ExtentReportManager.removeTest();
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

        // Finalize and write Extent Report
        ExtentReportManager.flushReport();
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
