package com.AutoPOC.base;

import com.AutoPOC.utils.context.TestContextManager;
import com.AutoPOC.utils.core.DriverFactory;
import com.AutoPOC.utils.reporting.ExtentReportManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.Map;

/**
 * Abstract base class for all Page Object classes.
 * <p>
 * Provides core Selenium WebDriver functionalities, including dynamic waits,
 * element interactions, and integrated logging for automation scalability.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    private static final int DEFAULT_TIMEOUT = 10;

    /**
     * Constructor initializes the WebDriver instance and binds PageFactory elements.
     */
    protected BasePage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    /** -------------------------------------------------
     *  Core Utility Methods
     *  ------------------------------------------------- */

    /**
     * Logs an informational message to the Extent Report.
     *
     * @param message The message to log
     */
    protected void log(String message) {
        ExtentReportManager.INSTANCE.logInfo(message, this.getClass());
    }

    /**
     * Retrieves input data from the Test Context.
     *
     * @return Map of input key-value pairs
     */
    protected Map<String, String> getInputData() {
        return TestContextManager.getInputData();
    }

    /** -------------------------------------------------
     *  Web Element Interaction Methods
     *  ------------------------------------------------- */

    /**
     * Clicks a WebElement after ensuring it is clickable.
     *
     * @param element The WebElement to click
     * @param logMsg  The message to log post-click
     */
    public void click(WebElement element, String logMsg) {
        waitUntilClickable(element, DEFAULT_TIMEOUT).click();
        log(logMsg);
    }

    /**
     * Clicks an element dynamically using a formatted XPath.
     *
     * @param fieldName     Logical field name (for logging)
     * @param rawValue      Dynamic value to inject into the XPath template
     * @param xpathTemplate XPath template containing a placeholder
     */
    protected void clickBy(String fieldName, String rawValue, String xpathTemplate) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is missing!");
        }
        String xpath = String.format(xpathTemplate, rawValue.trim());
        click(driver.findElement(By.xpath(xpath)), "Clicked " + fieldName + ": " + rawValue);
    }

    /**
     * Sends input text to a WebElement after clearing any existing content.
     *
     * @param element The WebElement to send input to
     * @param text    The text to input
     */
    protected void sendKeys(WebElement element, String text) {
        WebElement visibleElement = waitUntilVisible(element, DEFAULT_TIMEOUT);
        visibleElement.clear();
        visibleElement.sendKeys(text);
    }

    /**
     * Selects a dropdown option by its visible text.
     *
     * @param dropdown The dropdown WebElement
     * @param text     Visible text of the option to select
     */
    protected void selectByVisibleText(WebElement dropdown, String text) {
        new Select(dropdown).selectByVisibleText(text);
    }

    /** -------------------------------------------------
     *  Wait and Verification Methods
     *  ------------------------------------------------- */

    /**
     * Waits until a WebElement becomes clickable using default timeout.
     *
     * @param element The WebElement to wait for
     * @return The clickable WebElement
     */
    protected WebElement waitUntilClickable(WebElement element) {
        return waitUntilClickable(element, DEFAULT_TIMEOUT);
    }

    /**
     * Waits until a WebElement becomes clickable within a custom timeout.
     *
     * @param element WebElement to wait for
     * @param timeout Timeout in seconds
     * @return The clickable WebElement
     */
    protected WebElement waitUntilClickable(WebElement element, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits until a WebElement is visible using default timeout.
     *
     * @param element The WebElement to wait for
     * @return The visible WebElement
     */
    protected WebElement waitUntilVisible(WebElement element) {
        return waitUntilVisible(element, DEFAULT_TIMEOUT);
    }

    /**
     * Waits until a WebElement is visible within a custom timeout.
     *
     * @param element WebElement to wait for
     * @param timeout Timeout in seconds
     * @return The visible WebElement
     */
    protected WebElement waitUntilVisible(WebElement element, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits until specific text is present within a WebElement.
     *
     * @param element WebElement to check
     * @param text    Expected text
     * @param timeout Timeout in seconds
     */
    protected void waitUntilTextPresent(WebElement element, String text, int timeout) {
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    /**
     * Checks whether a WebElement is displayed within the default timeout.
     *
     * @param element The WebElement to verify
     * @return true if displayed; false otherwise
     */
    public boolean isDisplayed(WebElement element) {
        try {
            waitUntilVisible(element, DEFAULT_TIMEOUT);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Waits for a WebElement to disappear from the page.
     *
     * @param element The WebElement to wait for invisibility
     * @return true if the element disappears; false otherwise
     */
    public boolean waitUntilElementGone(WebElement element) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** -------------------------------------------------
     *  Specific Page Interaction Helpers
     *  ------------------------------------------------- */

    /**
     * Retrieves the number of address sections currently displayed on the page.
     *
     * @return The count of address sections
     */
    public int getNumberOfAddresses() {
        return driver.findElements(By.xpath("//div[@class='address-list']//div[contains(@class, 'section')]")).size();
    }
}
