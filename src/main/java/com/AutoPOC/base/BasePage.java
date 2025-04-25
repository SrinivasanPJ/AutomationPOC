package com.AutoPOC.base;

import com.AutoPOC.utils.core.DriverFactory;
import com.AutoPOC.utils.reporting.ExtentReportManager;
import com.AutoPOC.utils.context.TestContextManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.Map;

public abstract class BasePage {

    protected final WebDriver driver;

    protected BasePage() {
        this.driver = DriverFactory.getDriver();
        PageFactory.initElements(driver, this);
    }

    protected void log(String message) {
        ExtentReportManager.logInfo(message, this.getClass());
    }

    protected void click(WebElement element, String logMsg) {
        waitUntilClickable(element).click();
        log(logMsg);
    }

    protected void clickBy(String fieldName, String rawValue, String xpathTemplate) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is missing!");
        }
        String xpath = String.format(xpathTemplate, rawValue.trim());
        click(driver.findElement(By.xpath(xpath)), "Clicked " + fieldName + ": " + rawValue);
    }

    protected void sendKeys(WebElement element, String text) {
        waitUntilVisible(element).clear();
        element.sendKeys(text);
    }

    protected void selectByVisibleText(WebElement dropdown, String text) {
        new Select(dropdown).selectByVisibleText(text);
    }

    protected boolean isDisplayed(WebElement element, int timeout) {
        try {
            waitUntilVisible(element);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected WebElement waitUntilClickable(WebElement element) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    protected WebElement waitUntilVisible(WebElement element) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOf(element));
    }

    protected void waitUntilTextPresent(WebElement element, String text, int timeout) {
        new WebDriverWait(driver, Duration.ofSeconds(timeout))
                .until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    public boolean waitUntilElementGone(WebElement element) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected Map<String, String> getInputData() {
        return TestContextManager.getInputData();
    }

    public int getNumberOfAddresses() {
        return driver.findElements(By.xpath("//div[@class='address-list']//div[contains(@class, 'section')]")).size();
    }
}
