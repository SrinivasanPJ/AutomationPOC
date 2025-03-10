package com.AutoPOC;

import com.AutoPOC.utils.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.NoSuchElementException;

/**
 * BasePage class that provides common utility methods for page interactions.
 */
public class BasePage {
    private static final int DEFAULT_TIMEOUT = 6;
    protected WebDriver driver;

    public BasePage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    /**
     * Waits for an element to be clickable and clicks on it.
     */
    public void click(WebElement element) {
        waitForElementToBeClickable(element, DEFAULT_TIMEOUT);
        element.click();
    }

    /**
     * Clears an input field and sends text.
     */
    public void sendKeys(WebElement element, String text) {
        waitForElementToBeVisible(element, DEFAULT_TIMEOUT);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Refreshes the current page.
     */
    public void refreshPage() {
        driver.navigate().refresh();
    }

    /**
     * Scrolls to a specific element.
     */
    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    /**
     * Scrolls down the page by 500 pixels.
     */
    public void scrollDown() {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,500);");
    }

    /**
     * Scrolls up the page by 500 pixels.
     */
    public void scrollUp() {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0,-500);");
    }

    /**
     * Switches to a frame using its locator.
     */
    public void switchToFrame(By locator) {
        WebElement frameElement = driver.findElement(locator);
        driver.switchTo().frame(frameElement);
    }

    /**
     * Switches back to the default frame.
     */
    public void switchToDefaultFrame() {
        driver.switchTo().defaultContent();
    }

    /**
     * Accepts an alert if present.
     */
    public void acceptAlert() {
        waitForAlert(DEFAULT_TIMEOUT).accept();
    }

    /**
     * Dismisses an alert if present.
     */
    public void dismissAlert() {
        waitForAlert(DEFAULT_TIMEOUT).dismiss();
    }

    /**
     * Retrieves text from an alert.
     */
    public String getAlertText() {
        return waitForAlert(DEFAULT_TIMEOUT).getText();
    }

    /**
     * Waits for an element to be clickable.
     */
    public void waitForElementToBeClickable(WebElement element, int timeout) {
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits for an element to be visible.
     */
    public void waitForElementToBeVisible(WebElement element, int timeout) {
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits for an element to be present in the DOM.
     */
    public void waitForElementToBePresent(By locator, int timeout) {
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Waits for text to be present in an element.
     */
    public void waitForTextToBePresent(WebElement element, String text, int timeout) {
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    /**
     * Waits for an element to become stale.
     */
    public void waitForElementToBeStale(WebElement element, int timeout) {
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.stalenessOf(element));
    }

    /**
     * Performs a fluent wait on an element.
     */
    public WebElement fluentWait(By locator, int timeout, int polling) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(polling))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class)
                .until(d -> d.findElement(locator));
    }

    /**
     * Attempts to click an element with retries.
     */
    public boolean clickElementWithRetry(WebElement element, int retries) {
        int attempts = 0;
        while (attempts < retries) {
            try {
                waitForElementToBeClickable(element, 5);
                element.click();
                return true;
            } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
                attempts++;
            }
        }
        return false;
    }

    /**
     * Waits for an alert to be present.
     */
    private Alert waitForAlert(int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.alertIsPresent());
    }
}
