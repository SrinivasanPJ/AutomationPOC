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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

/**
 * Factory utility for initializing and managing WebDriver instances.
 * Supports Chrome, Firefox, and Edge with argument and headless configurations.
 */
public class DriverFactory {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);
    private static final String DEFAULT_BROWSER = "chrome";

    /**
     * Initializes the WebDriver based on configuration or input.
     * @param browserFromExcel Optional browser name (overrides config).
     */
    public static void initializeDriver(String browserFromExcel) {
        if (driver.get() != null) return;

        String browser = Optional.ofNullable(browserFromExcel)
                .filter(s -> !s.isBlank())
                .orElse(ConfigReader.getProperty("browser", DEFAULT_BROWSER))
                .trim().toLowerCase();

        boolean isHeadless = Boolean.parseBoolean(ConfigReader.getProperty("headless.mode", "false"));

        List<String> browserArgs = getArgs(browser + ".browser.arguments");
        List<String> headlessArgs = getArgs("headless.arguments");

        WebDriver webDriver = getBrowserMap(browserArgs, headlessArgs, isHeadless)
                .getOrDefault(browser, getBrowserMap(browserArgs, headlessArgs, isHeadless).get(DEFAULT_BROWSER))
                .get();

        driver.set(webDriver);
        logger.info("WebDriver initialized for browser: {}", browser);
    }

    private static List<String> getArgs(String configKey) {
        String argString = ConfigReader.getProperty(configKey, "");
        return argString.isBlank() ? Collections.emptyList() : Arrays.asList(argString.split(","));
    }

    private static Map<String, Supplier<WebDriver>> getBrowserMap(List<String> browserArgs, List<String> headlessArgs, boolean isHeadless) {
        return Map.of(
                "chrome", () -> createDriver(new ChromeOptions(), WebDriverManager.chromedriver(), browserArgs, headlessArgs, isHeadless, ChromeDriver::new),
                "firefox", () -> createDriver(new FirefoxOptions(), WebDriverManager.firefoxdriver(), browserArgs, headlessArgs, isHeadless, FirefoxDriver::new),
                "edge", () -> createDriver(new EdgeOptions(), WebDriverManager.edgedriver(), browserArgs, headlessArgs, isHeadless, EdgeDriver::new)
        );
    }

    private static <T extends WebDriver, O extends MutableCapabilities>
    T createDriver(O options, WebDriverManager manager, List<String> browserArgs,
                   List<String> headlessArgs, boolean isHeadless, Supplier<T> driverSupplier) {

        manager.clearDriverCache();
        manager.setup();

        applyArguments(options, browserArgs, headlessArgs, isHeadless);
        T webDriver = driverSupplier.get();

        try {
            webDriver.manage().window().maximize();
            logger.info("Browser window maximized explicitly.");
        } catch (Exception e) {
            logger.warn("Unable to maximize browser window: {}", e.getMessage());
        }

        return webDriver;
    }

    private static void applyArguments(MutableCapabilities options, List<String> browserArgs,
                                       List<String> headlessArgs, boolean isHeadless) {
        if (options instanceof ChromeOptions chromeOptions) {
            chromeOptions.addArguments(browserArgs);
            if (isHeadless) chromeOptions.addArguments(headlessArgs);

            chromeOptions.addArguments("--incognito", "--disable-popup-blocking", "--disable-notifications",
                    "--disable-blink-features=AutomationControlled", "--disable-infobars", "--disable-extensions");

            Map<String, Object> prefs = Map.of(
                    "credentials_enable_service", false,
                    "profile.password_manager_enabled", false
            );

            chromeOptions.setExperimentalOption("prefs", prefs);
            chromeOptions.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
            chromeOptions.setExperimentalOption("useAutomationExtension", false);
        } else {
            if (options instanceof FirefoxOptions ff) {
                ff.addArguments(browserArgs);
                if (isHeadless) ff.addArguments(headlessArgs);
            } else if (options instanceof EdgeOptions edge) {
                edge.addArguments(browserArgs);
                if (isHeadless) edge.addArguments(headlessArgs);
            }
        }

        logger.info("Applying browser arguments: {}", browserArgs);
    }

    /**
     * Returns the active WebDriver instance for the thread.
     * @return current thread's WebDriver
     */
    public static WebDriver getDriver() {
        if (driver.get() == null)
            throw new IllegalStateException("WebDriver not initialized! Call initializeDriver() first.");
        return driver.get();
    }

    /**
     * Quits the WebDriver and removes it from the thread context.
     */
    public static void quitDriver() {
        if (driver.get() != null) {
            try {
                driver.get().manage().deleteAllCookies();
                logger.info("Cleared all cookies for the domain: {}", getDomain(driver.get().getCurrentUrl()));
                driver.get().quit();
                logger.info("WebDriver quit successfully.");
            } catch (Exception e) {
                logger.error("Error while quitting WebDriver: {}", e.getMessage(), e);
            } finally {
                driver.remove();
            }
        }
    }

    /**
     * Extracts the protocol and host from a full URL.
     * @param url full browser URL
     * @return domain portion of the URL
     */
    private static String getDomain(String url) {
        try {
            URL parsedUrl = new URL(url);
            return parsedUrl.getProtocol() + "://" + parsedUrl.getHost();
        } catch (MalformedURLException e) {
            logger.error("Error parsing URL: {}", e.getMessage(), e);
            return url; // fallback
        }
    }
}