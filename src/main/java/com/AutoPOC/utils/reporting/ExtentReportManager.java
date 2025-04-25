package com.AutoPOC.utils.reporting;

import com.AutoPOC.config.ConfigReader;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.reporter.configuration.ViewName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to manage Extent Reports for test automation logging.
 */
public class ExtentReportManager {

    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(ExtentReportManager.class);
    private static ExtentReports extent;

    /**
     * Initializes the Extent report and configures its visual and metadata settings.
     */
    public static void initReport() {
        if (extent == null) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String reportPath = "reports/ExecutionReport_" + timestamp + ".html";

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setReportName("Automation Execution Report");
            spark.config().setDocumentTitle("Demo WebShop Report");
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setTimelineEnabled(true);

            // Set view order (side panel customization)
            spark.viewConfigurer().viewOrder()
                    .as(new ViewName[]{
                            ViewName.DASHBOARD,
                            ViewName.TEST,
                            ViewName.CATEGORY,
                            ViewName.AUTHOR,
                            ViewName.EXCEPTION,
                            ViewName.LOG
                    });

            extent = new ExtentReports();
            extent.attachReporter(spark);

            // System Info
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("Time Zone", System.getProperty("user.timezone"));
            extent.setSystemInfo("Browser", ConfigReader.getProperty("browser"));
            extent.setSystemInfo("Environment", ConfigReader.getProperty("env.name", "Unknown"));
            extent.setSystemInfo("Build Version", ConfigReader.getProperty("build.version", "N/A"));
        }
    }

    /**
     * Creates a new test entry in the report.
     * @param testName The name of the test.
     */
    public static void createTest(String testName) {
        ExtentTest test = extent.createTest(testName);
        extentTest.set(test);
    }

    /**
     * Returns the current test instance associated with the calling thread.
     * @return The current ExtentTest instance.
     */
    public static ExtentTest getTest() {
        return extentTest.get();
    }

    /**
     * Logs a passing test step.
     * @param message The message to log.
     */
    public static void logPass(String message) {
        getTest().pass(message);
        logger.info(message);
    }

    /**
     * Logs a failing test step and provides a link to the automation log.
     * @param message The failure message.
     */
    public static void logFail(String message) {
        getTest().fail(message + "<br><a href='../logs/automation.log' target='_blank'>📄 View automation.log</a>");
        logger.error(message);
    }

    /**
     * Logs a skipped test step.
     * @param message The skip reason.
     */
    public static void logSkip(String message) {
        getTest().skip(message);
        logger.warn(message);
    }

    /**
     * Logs an informational message with class context.
     * @param message The message to log.
     * @param clazz The class from which the log originates.
     */
    public static void logInfo(String message, Class<?> clazz) {
        getTest().info(message);
        LoggerFactory.getLogger(clazz).info(message);
    }

    /**
     * Logs an informational message without class context.
     * @param message The message to log.
     */
    public static void logInfoSimple(String message) {
        getTest().info(message);
        logger.info(message);
    }

    /**
     * Logs a custom status message with class context.
     * @param status The status (PASS, FAIL, INFO, etc.)
     * @param message The message to log.
     * @param clazz The originating class.
     */
    public static void log(Status status, String message, Class<?> clazz) {
        if (extentTest.get() != null) {
            extentTest.get().log(status, "[" + clazz.getSimpleName() + "] " + message);
        }
    }

    /**
     * Attaches a screenshot to the current test log.
     * @param screenshotPath The file path to the screenshot.
     * @param title The title or caption for the screenshot.
     */
    public static void attachScreenshotFromPath(String screenshotPath, String title) {
        try {
            String relativePath = screenshotPath.replace("screenshots/", "../screenshots/");
            getTest().info(title).addScreenCaptureFromPath(relativePath);
        } catch (Exception e) {
            logger.error("Failed to attach screenshot to report", e);
        }
    }

    /**
     * Flushes the report data to disk.
     */
    public static void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }

    /**
     * Removes the current test instance from the thread-local context.
     */
    public static void removeTest() {
        extentTest.remove();
    }
}
