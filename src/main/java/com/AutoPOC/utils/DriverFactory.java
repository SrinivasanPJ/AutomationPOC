package com.AutoPOC.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory class for WebDriver initialization and management.
 */
public class DriverFactory {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);
    private static final String DEFAULT_BROWSER = "chrome";

    private static List<String> getBrowserArguments(String browser) {
        return parseArguments(ConfigReader.getProperty(browser + ".browser.arguments", ""));
    }

    private static List<String> getHeadlessArguments() {
        return parseArguments(ConfigReader.getProperty("headless.arguments", ""));
    }

    private static List<String> parseArguments(String arguments) {
        return arguments.isEmpty() ? List.of() : Arrays.asList(arguments.split(","));
    }

    public static void initializeDriver(String browserFromExcel) {
        if (driver.get() != null) return;

        String browser = (browserFromExcel != null && !browserFromExcel.isEmpty())
                ? browserFromExcel.toLowerCase()
                : ConfigReader.getProperty("browser", DEFAULT_BROWSER).trim().toLowerCase();

        boolean isHeadless = Boolean.parseBoolean(ConfigReader.getProperty("headless.mode", "false"));

        List<String> browserArgs = getBrowserArguments(browser); // Pass the browser name
        List<String> headlessArgs = getHeadlessArguments();

        driver.set(getBrowserMap(browserArgs, headlessArgs, isHeadless)
                .getOrDefault(browser, getBrowserMap(browserArgs, headlessArgs, isHeadless).get("chrome"))
                .get());

        logger.info("WebDriver initialized for browser: {}", browser);
    }

    private static Map<String, Supplier<WebDriver>> getBrowserMap(List<String> browserArgs, List<String> headlessArgs, boolean isHeadless) {
        return Map.of(
                "firefox", () -> createWebDriver(new FirefoxOptions(), WebDriverManager.firefoxdriver(), browserArgs, headlessArgs, isHeadless, FirefoxDriver::new),
                "edge", () -> createWebDriver(new EdgeOptions(), WebDriverManager.edgedriver(), browserArgs, headlessArgs, isHeadless, EdgeDriver::new),
                "chrome", () -> createWebDriver(new ChromeOptions(), WebDriverManager.chromedriver(), browserArgs, headlessArgs, isHeadless, ChromeDriver::new)
        );
    }

    private static <T extends WebDriver, O extends MutableCapabilities>
    T createWebDriver(O options, WebDriverManager manager, List<String> browserArgs,
                      List<String> headlessArgs, boolean isHeadless, Supplier<T> driverSupplier) {

        if (manager.getClass().equals(WebDriverManager.chromedriver().getClass())) {
            manager.clearDriverCache();
        }

        manager.setup(); // Ensures latest driver is used

        // Apply browser-specific arguments
        if (options instanceof ChromeOptions chromeOptions) {
            chromeOptions.addArguments(browserArgs);
            if (isHeadless) chromeOptions.addArguments(headlessArgs);
        } else if (options instanceof FirefoxOptions firefoxOptions) {
            firefoxOptions.addArguments(browserArgs);
            if (isHeadless) firefoxOptions.addArguments(headlessArgs);
        } else if (options instanceof EdgeOptions edgeOptions) {
            edgeOptions.addArguments(browserArgs);
            if (isHeadless) edgeOptions.addArguments(headlessArgs);
        }

        logger.info("Applying browser arguments: {}", browserArgs);

        T webDriver = driverSupplier.get();

        // **Force maximization regardless of browser settings**
        try {
            webDriver.manage().window().maximize();
            logger.info("Browser window maximized explicitly.");
        } catch (Exception e) {
            logger.warn("Unable to maximize browser window: {}", e.getMessage());
        }

        return webDriver;
    }

    public static WebDriver getDriver() {
        if (driver.get() == null)
            throw new IllegalStateException("WebDriver not initialized! Call initializeDriver() first.");
        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            try {
                driver.get().manage().deleteAllCookies();
                logger.info("Cleared cookies for the current URL: {}", driver.get().getCurrentUrl());

                driver.get().quit();
                logger.info("WebDriver quit successfully.");
            } catch (Exception e) {
                logger.error("Error while quitting WebDriver: {}", e.getMessage(), e);
            } finally {
                driver.remove();
            }
        }
    }
}
