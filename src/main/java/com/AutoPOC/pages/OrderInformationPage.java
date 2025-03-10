package com.AutoPOC.pages;

import com.AutoPOC.BasePage;
import com.AutoPOC.utils.ConfigReader;
import com.AutoPOC.utils.OrderDataUtil;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderInformationPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(OrderInformationPage.class);

    // Fetch values from config.properties
    private static final String filePath = ConfigReader.getProperty("Transactional_Data_File_Path");
    private static final String sheetName = ConfigReader.getProperty("Transactional_Data_Sheet_Name");

    @FindBy(xpath = "//a[normalize-space()='Click here for order details.']")
    private WebElement orderDetailsPage;

    @FindBy(css = "div[class='order-number'] strong")
    private WebElement orderId;

    @FindBy(css = "div[class='order-overview'] span:nth-child(1)")
    private WebElement orderDate;

    @FindBy(css = "div[class='master-wrapper-main'] span:nth-child(2)")
    private WebElement orderStatus;

    /**
     * Clicks on the "Order Details" link to navigate to the order details page.
     */
    public void clickOrderDetailsLink() {
        click(orderDetailsPage);
        logger.info("Order details link clicked");
    }

    /**
     * Retrieves the Order ID from the order details page.
     *
     * @return the extracted Order ID as a String. If no valid ID is found, returns an empty string.
     */
    public String getOrderId() {
        waitForElementToBeVisible(orderId, 4);
        logger.info("Getting order ID...");
        String text = orderId.getText().trim();
        int index = text.indexOf("#");
        return (index != -1) ? text.substring(index + 1).trim() : "";
    }

    /**
     * Retrieves the Order Date from the order details page.
     * The date is extracted and formatted into "MM/dd/yyyy" format.
     *
     * @return the formatted Order Date as a String.
     * If parsing fails, an assertion error is triggered.
     */
    public String getOrderDate() {
        try {
            waitForElementToBeVisible(orderDate, 4);
            String datePart = orderDate.getText().trim().replaceAll("Order Date: \\w+, ", "");
            Date date = new SimpleDateFormat("MMMM d, yyyy").parse(datePart);
            return new SimpleDateFormat("MM/dd/yyyy").format(date);
        } catch (Exception e) {
            Assert.fail("Invalid date");
        }
        return "";
    }

    /**
     * Retrieves the Order Status from the order details page.
     *
     * @return the extracted Order Status as a String.
     */
    public String getOrderStatus() {
        waitForElementToBeVisible(orderStatus, 4);
        return orderStatus.getText().trim().replace("Order Status: ", "").trim();
    }

    /**
     * Saves the extracted order details (Order ID, Order Date, and Order Status)
     * into the specified Excel sheet.
     */
    public void saveOrderDetailsToExcelFile() {
        String orderNum = getOrderId();
        String orderDate = getOrderDate();
        String orderStatus = getOrderStatus();
        logger.info("{} | {} | {}", orderNum, orderDate, orderStatus);
        OrderDataUtil.writeOrderData(filePath, sheetName, orderNum, orderDate, orderStatus);
    }

}
