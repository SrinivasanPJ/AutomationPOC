package com.AutoPOC.pages;

import com.AutoPOC.BasePage;
import com.AutoPOC.utils.OrderDataUtil;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Page object to represent the Order Information screen.
 * Responsible for retrieving Order ID and Date after checkout
 * and saving them to the Excel result sheet.
 */
public class OrderInformationPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(OrderInformationPage.class);

    @FindBy(xpath = "//a[normalize-space()='Click here for order details.']")
    private WebElement orderDetailsLink;

    @FindBy(css = "div.order-number strong")
    private WebElement orderIdElem;

    @FindBy(css = "div.order-overview span:nth-child(1)")
    private WebElement orderDateElem;

    /**
     * Clicks the link to navigate to the order details section.
     */
    public void clickOrderDetailsLink() {
        click(orderDetailsLink, "Order details link clicked");
    }

    /**
     * Retrieves the Order ID from the order confirmation page.
     *
     * @return String order ID without the '#' prefix
     */
    public String getOrderId() {
        waitUntilVisible(orderIdElem, 5);
        String txt = orderIdElem.getText().trim();
        int i = txt.indexOf('#');
        return i >= 0 ? txt.substring(i + 1).trim() : txt;
    }

    /**
     * Parses and formats the order date into MM/dd/yyyy format.
     *
     * @return Formatted order date
     */
    public String getOrderDate() {
        try {
            waitUntilVisible(orderDateElem, 5);
            String raw = orderDateElem.getText().replaceAll("Order Date: \\w+, ", "").trim();
            Date dt = new SimpleDateFormat("MMMM d, yyyy").parse(raw);
            return new SimpleDateFormat("MM/dd/yyyy").format(dt);
        } catch (Exception e) {
            Assert.fail("Invalid order date", e);
            return "";
        }
    }

    /**
     * Writes Order ID and Date to the Excel transactional sheet for tracking.
     *
     * @param rowIndex Row index where order details should be written
     */
    public void saveDetailsToExcel(int rowIndex) {
        String orderId = getOrderId();
        String orderDate = getOrderDate();
        logger.info("Saving to Excel → ID={}  Date={}", orderId, orderDate);
        OrderDataUtil.writeOrderData(orderId, orderDate, rowIndex);
    }
}
