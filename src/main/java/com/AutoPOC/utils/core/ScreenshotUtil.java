package com.AutoPOC.utils.core;

import com.AutoPOC.utils.reporting.LogUtil;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {

    /**
     * Captures screenshot as Base64 string for embedding in Extent Report.
     *
     * @param driver WebDriver instance
     * @return Base64 encoded screenshot
     */
    public static String captureScreenshotAsBase64(WebDriver driver) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        return ts.getScreenshotAs(OutputType.BASE64);
    }

    /**
     * Saves a PNG screenshot to the /screenshots directory with test name + timestamp.
     *
     * @param driver   WebDriver instance
     * @param testName Name of the test case
     * @return Full file path to the saved screenshot
     */
    public static String saveScreenshotAsPNG(WebDriver driver, String testName) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String screenshotPath = "screenshots/" + testName + "_" + timestamp + ".png";

            File dest = new File(screenshotPath);
            FileUtils.copyFile(src, dest);
            return screenshotPath;
        } catch (IOException e) {
            LogUtil.error(ScreenshotUtil.class, "Failed to capture screenshot", e);
            return null;
        }
    }
}
