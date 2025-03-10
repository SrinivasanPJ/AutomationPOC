package com.AutoPOC;

import com.AutoPOC.pages.AddProductsToCartAndPlaceOrder;
import com.AutoPOC.pages.LoginPage;
import com.AutoPOC.pages.OrderInformationPage;
import com.AutoPOC.utils.ConfigReader;
import com.AutoPOC.utils.DriverFactory;
import com.AutoPOC.utils.TestDataUtil;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * BaseTest class for setting up test execution, driver initialization, and teardown.
 */
public class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    private static final String TEST_DATA_FILE = ConfigReader.getProperty("Test_Data_File_Path");
    private static Instant startTime;
    protected WebDriver driver;
    protected LoginPage loginPage;
    protected AddProductsToCartAndPlaceOrder addProductsToCartAndPlaceOrder;
    protected OrderInformationPage orderInformationPage;
    private String username;
    private String password;

    /**
     * Logs test suite execution start time.
     */
    @BeforeSuite
    public void suiteSetup() {
        startTime = Instant.now();
        logger.info("Test Execution Started at: {}", getCurrentTime());
    }

    /**
     * Loads login credentials from test data before the test class execution.
     */
    @BeforeClass
    public void setUpTestData() {
        Object[][] loginData = TestDataUtil.getLoginDetails(TEST_DATA_FILE, "Login");

        if (loginData.length == 0) {
            logger.error("No login data found in test data file: {}", TEST_DATA_FILE);
            throw new RuntimeException("No login data found in the test data file.");
        }

        username = (String) loginData[0][1];
        password = (String) loginData[0][2];
        logger.info("Login credentials loaded successfully.");
    }

    /**
     * Initializes WebDriver before each test method execution.
     */
    @BeforeMethod
    public void setUp() {
        logger.info("Setting up WebDriver before test execution.");
    }

    /**
     * Executes a test for a specific TestID.
     */
    @Test(dataProvider = "testData")
    public void executeTestForTestID(String testID, ITestContext context) {
        logger.info("Fetching test data for Test ID: {}", testID);
        Map<String, String> testData = TestDataUtil.getTestCaseByTestID("Login", testID);

        if (testData == null) {
            logger.error("Test ID '{}' not found in Excel.", testID);
            throw new RuntimeException("TestID " + testID + " not found in Excel!");
        }

        String browser = testData.getOrDefault("browser", "chrome");
        String testURL = testData.getOrDefault("URL", "about:blank");
        String testUsername = testData.getOrDefault("username", username);
        String testPassword = testData.getOrDefault("password", password);

        context.setAttribute("TestID", testID);
        context.setAttribute("Browser", browser);

        logger.info("Running Test ID: {} on Browser: {}", testID, browser);

        try {
            DriverFactory.initializeDriver(browser);
            driver = DriverFactory.getDriver();
            driver.get(testURL);
            logger.info("Navigated to URL: {}", testURL);
        } catch (Exception e) {
            logger.error("WebDriver initialization failed: {}", e.getMessage(), e);
            throw new RuntimeException("WebDriver initialization failed: " + e.getMessage());
        }

        loginPage = new LoginPage();
        addProductsToCartAndPlaceOrder = new AddProductsToCartAndPlaceOrder();
        orderInformationPage = new OrderInformationPage();

        try {
            loginPage.login(testUsername, testPassword);
            logger.info("Successfully logged in as: {}", testUsername);
        } catch (Exception e) {
            logger.error("Login failed for user: {}", testUsername, e);
            throw new RuntimeException("Login failed for user: " + testUsername);
        }
    }

    /**
     * Logs the end time of test suite execution and calculates execution time.
     */
    @AfterSuite
    public void suiteTearDown() {
        logger.info("Test Execution Ended at: {}", getCurrentTime());
        logger.info("Total Execution Time: {}", getExecutionDuration());
    }

    /**
     * Provides test data from Excel for data-driven tests.
     */
    @DataProvider(name = "testData")
    public Object[][] getTestData() {
        Object[][] data = TestDataUtil.getLoginDetails(TEST_DATA_FILE, "Login");
        Object[][] testIDs = new Object[data.length][1];
        for (int i = 0; i < data.length; i++) {
            testIDs[i][0] = data[i][0];
        }
        return testIDs;
    }

    /**
     * Cleans up WebDriver instance after each test method execution.
     */
    @AfterMethod
    public void tearDown() {
        try {
            DriverFactory.quitDriver(); // Calls quitDriver, which already logs success
        } catch (Exception e) {
            logger.error("Error while quitting WebDriver: {}", e.getMessage(), e);
        }
    }


    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String getExecutionDuration() {
        Duration duration = Duration.between(startTime, Instant.now());
        return String.format("%02d min, %02d sec", duration.toMinutes(), duration.getSeconds() % 60);
    }
}
