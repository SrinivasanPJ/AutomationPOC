package com.AutoPOC.base;

import com.AutoPOC.config.ConfigReader;
import com.AutoPOC.pages.AddProductsToCartAndPlaceOrder;
import com.AutoPOC.pages.LoginPage;
import com.AutoPOC.pages.OrderInformationPage;
import com.AutoPOC.utils.context.TestContextManager;
import com.AutoPOC.utils.context.TestDataKeys;
import com.AutoPOC.utils.core.DriverFactory;
import com.AutoPOC.utils.core.PopupHandler;
import com.AutoPOC.utils.core.ScreenshotUtil;
import com.AutoPOC.utils.data.ExecutionDataUtil;
import com.AutoPOC.utils.data.SyntheticDataUtil;
import com.AutoPOC.utils.data.TestDataUtil;
import com.AutoPOC.utils.excel.ExcelColumnIndex;
import com.AutoPOC.utils.excel.ExcelReaderUtil;
import com.AutoPOC.utils.excel.ExcelUtil;
import com.AutoPOC.utils.reporting.EmailSenderUtil;
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
 * Abstract base class for all TestNG test classes.
 * <p>
 * Manages WebDriver initialization, test execution workflow,
 * ExtentReport integration, data recording, and teardown procedures.
 */
public class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    private static Instant startTime;

    protected WebDriver driver;
    protected LoginPage loginPage;
    protected AddProductsToCartAndPlaceOrder addProductsToCartAndPlaceOrder;
    protected OrderInformationPage orderInformationPage;

    /** -------------------------------------------------
     *  Test Suite Setup and Teardown
     *  ------------------------------------------------- */

    /**
     * Executes once before the entire test suite.
     * Initializes ExtentReports and records start time.
     */
    @BeforeSuite
    public void suiteSetup() {
        startTime = Instant.now();
        logger.info("Test Execution Started at: {}", getCurrentTime());
        ExtentReportManager.INSTANCE.initReport();
    }

    /**
     * Executes once after the entire test suite.
     * Flushes reports, sends email notifications, and logs execution summary.
     */
    @AfterSuite
    public void suiteTearDown() {
        ExtentReportManager.INSTANCE.flushReport();

        EmailSenderUtil.sendTestResultEmail(
                ExtentReportManager.INSTANCE.getTotalTests(),
                ExtentReportManager.INSTANCE.getTestsPassed(),
                ExtentReportManager.INSTANCE.getTestsFailed()
        );

        logger.info("Test Execution Ended at: {}", getCurrentTime());
        logger.info("Total Execution Time: {}", getExecutionDuration());
    }

    /** -------------------------------------------------
     *  Test Setup and Execution
     *  ------------------------------------------------- */

    /**
     * Executes before each test method.
     * Initializes the ExtentReport test node and logs setup actions.
     *
     * @param method The test method about to be executed
     */
    @BeforeMethod
    public void setUp(Method method) {
        ExtentReportManager.INSTANCE.createTest(method.getName());
        logger.info("Setting up WebDriver before test execution.");
    }

    /**
     * Core setup and execution flow for each TestID.
     * Initializes WebDriver, navigates to application, logs into the system, and prepares page objects.
     *
     * @param testID  The Test ID to execute
     * @param context TestNG context object
     */
    @Test(dataProvider = "testData")
    public void executeTestForTestID(String testID, ITestContext context) {
        logger.info("Fetching test data for TestID: {}", testID);

        Map<String, String> testData = TestDataUtil.getTestCaseByTestID(testID);
        if (testData.isEmpty()) {
            throw new RuntimeException("TestID " + testID + " not found in Excel!");
        }

        String browser = testData.getOrDefault(TestDataKeys.BROWSER, "chrome");
        String testURL = testData.getOrDefault(TestDataKeys.URL, "about:blank");
        String username = testData.get(TestDataKeys.USERNAME);
        String password = testData.get(TestDataKeys.PASSWORD);

        context.setAttribute("TestID", testID);
        context.setAttribute("Browser", browser);

        logger.info("Running TestID={} on browser={}", testID, browser);

        try {
            DriverFactory.initializeDriver(browser);
            driver = DriverFactory.getDriver();
            driver.get(testURL);
            logger.info("Navigated to: {}", testURL);
        } catch (Exception e) {
            logger.error("WebDriver initialization failed.", e);
            throw new RuntimeException(e);
        }

        loginPage = new LoginPage();
        addProductsToCartAndPlaceOrder = new AddProductsToCartAndPlaceOrder();
        orderInformationPage = new OrderInformationPage();

        try {
            loginPage.login(username, password);
            logger.info("Logged in as {}", username);
            PopupHandler.dismissSavePasswordPopup();
        } catch (Exception e) {
            logger.error("Login failed for user: {}", username, e);
            throw new RuntimeException(e);
        }
    }

    /** -------------------------------------------------
     *  Test Teardown and Reporting
     *  ------------------------------------------------- */

    /**
     * Executes after each test method.
     * Records execution data into Excel, handles reporting for success/failure, and captures screenshots on failure.
     *
     * @param result TestNG result object
     */
    @AfterMethod(alwaysRun = true)
    public void recordExecutionData(ITestResult result) {
        try {
            int rowIndex = getOrCreateExcelRowIndex(result);

            ExecutionDataUtil.writeExecutionData(rowIndex, result);

            switch (result.getStatus()) {
                case ITestResult.SUCCESS -> ExtentReportManager.INSTANCE.logPass("Test Passed: " + result.getName());
                case ITestResult.FAILURE -> {
                    ExtentReportManager.INSTANCE.logFail("Test Failed: " + result.getName(), result.getThrowable());
                    String screenshotPath = ScreenshotUtil.saveScreenshotAsPNG(driver, result.getName());
                    ExtentReportManager.INSTANCE.attachScreenshotFromPath(screenshotPath, "Failure Screenshot");
                    logger.error("Failure details:", result.getThrowable());
                }
                case ITestResult.SKIP -> ExtentReportManager.INSTANCE.logSkip("Test Skipped: " + result.getName());
            }
        } catch (Exception e) {
            logger.error("Error recording execution data.", e);
        } finally {
            ExtentReportManager.INSTANCE.removeTest();
        }
    }

    /**
     * Executes after each test method to quit the WebDriver instance.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        try {
            DriverFactory.quitDriver();
        } catch (Exception e) {
            logger.error("Error quitting WebDriver.", e);
        }
    }

    /**
     * Clears TestContext after each method to avoid data leakage between tests.
     */
    @AfterMethod
    public void clearContext() {
        TestContextManager.clear();
    }

    /** -------------------------------------------------
     *  Data Providers
     *  ------------------------------------------------- */

    /**
     * Provides test data mapped by TestIDs from external sources.
     *
     * @return Object array of TestIDs
     */
    @DataProvider(name = "testData")
    public Object[][] getTestData() {
        return TestDataUtil.getAllTestIDs();
    }

    /**
     * Provides synthetic data inputs for testing.
     *
     * @return Object array of synthetic input IDs
     */
    @DataProvider(name = "syntheticData")
    public Object[][] syntheticData() {
        return SyntheticDataUtil.getAllInputIDs();
    }

    /** -------------------------------------------------
     *  Private Utility Methods
     *  ------------------------------------------------- */

    /**
     * Returns the current timestamp as formatted string.
     *
     * @return Current timestamp (yyyy-MM-dd HH:mm:ss)
     */
    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Calculates total execution duration from start to end.
     *
     * @return Formatted execution duration string
     */
    private String getExecutionDuration() {
        Duration duration = Duration.between(startTime, Instant.now());
        return String.format("%02d min, %02d sec", duration.toMinutes(), duration.getSeconds() % 60);
    }

    /**
     * Retrieves or creates the Excel row index for recording test execution.
     *
     * @param result TestNG ITestResult instance
     * @return Row index to be used
     */
    private int getOrCreateExcelRowIndex(ITestResult result) {
        Object attr = result.getTestContext().getAttribute("ExcelRowIndex");

        if (attr instanceof Integer idx) {
            return idx;
        } else {
            Sheet sheet = ExcelReaderUtil.getSheet(
                    ConfigReader.getProperty("Test_Data_File_Path"),
                    ConfigReader.getProperty("Transactional_Data_Sheet_Name")
            );
            int newRow = ExcelUtil.findNextAvailableRow(sheet, ExcelColumnIndex.RUN_ID, 2);
            result.getTestContext().setAttribute("ExcelRowIndex", newRow);
            return newRow;
        }
    }
}
