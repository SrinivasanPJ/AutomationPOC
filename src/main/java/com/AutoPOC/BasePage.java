package com.AutoPOC;

import com.AutoPOC.utils.DriverFactory;
import com.AutoPOC.utils.TestContextManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * Abstract base class for all page objects.
 * Provides reusable methods for element interactions, wait conditions,
 * and other browser utilities.
 * Follows Page Object Model design principles.
 */
public abstract class BasePage {

    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    private static final int DEFAULT_TIMEOUT = 10;
    protected final WebDriver driver;

    /**
     * Initializes WebDriver instance and PageFactory elements.
     */
    protected BasePage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    // ========================= COMMON ACTIONS ========================= //

    protected void click(WebElement element, String logMsg) {
        waitUntilClickable(element).click();
        logger.info(logMsg);
    }

    protected void sendKeys(WebElement element, String text) {
        waitUntilVisible(element).clear();
        element.sendKeys(text);
    }

    protected void selectByVisibleText(WebElement dropdown, String text) {
        new Select(dropdown).selectByVisibleText(text);
    }

    // ========================= WAIT HELPERS ========================= //

    protected WebElement waitUntilVisible(WebElement element) {
        return waitUntilVisible(element, DEFAULT_TIMEOUT);
    }

    protected WebElement waitUntilVisible(WebElement element, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement waitUntilClickable(WebElement element) {
        return waitUntilClickable(element, DEFAULT_TIMEOUT);
    }

    protected WebElement waitUntilClickable(WebElement element, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    protected WebElement waitUntilClickable(By locator, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void waitUntilTextPresent(WebElement element, String text, int timeout) {
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    public void waitForUrlFragment(String fragment) {
        new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.urlContains(fragment));
    }

    public void waitUntilElementStale(WebElement element, int timeout) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeout))
                    .until(ExpectedConditions.stalenessOf(element));
            logger.info("Element became stale.");
        } catch (TimeoutException e) {
            logger.warn("Element did not become stale within {} seconds", timeout);
        }
    }

    public boolean waitUntilElementGone(WebElement element) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException e) {
            logger.warn("Element did not disappear in time");
            return false;
        }
    }

    // ========================= VALIDATIONS & RETRY ========================= //

    protected boolean isDisplayed(WebElement element, int timeout) {
        try {
            waitUntilVisible(element, timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void acceptAlert() {
        new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.alertIsPresent()).accept();
    }

    public void retryClick(WebElement element, String logMsg, String expectedUrlFragment, int maxRetries) {
        for (int i = 1; i <= maxRetries; i++) {
            try {
                click(element, logMsg + " (Attempt " + i + ")");
                Thread.sleep(1000);
                if (driver.getCurrentUrl().contains(expectedUrlFragment)) {
                    return;
                }
            } catch (Exception e) {
                logger.warn("Retry {} failed: {}", i, e.getMessage());
            }
        }
        throw new RuntimeException("Failed after " + maxRetries + " click attempts.");
    }

    public void clickBy(String fieldName, String rawValue, String xpathTemplate) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is missing!");
        }
        String xpath = String.format(xpathTemplate, rawValue.trim());
        click(driver.findElement(By.xpath(xpath)), "Clicked " + fieldName + ": " + rawValue);
    }

    // ========================= UTILITIES ========================= //

    public int getNumberOfAddresses() {
        return driver.findElements(By.xpath("//div[@class='address-list']//div[contains(@class, 'section')]")).size();
    }

    protected Map<String, String> getInputData() {
        return TestContextManager.getInputData();
    }
}
