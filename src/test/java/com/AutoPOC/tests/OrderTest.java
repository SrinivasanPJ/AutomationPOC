package com.AutoPOC.tests;

import com.AutoPOC.BaseTest;
import com.AutoPOC.utils.*;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Map;

public class OrderTest extends BaseTest {

    @Test(description = "Place order for a specific synthetic data row")
    @Parameters("inputID")
    public void addProductsToCart(String inputID, ITestContext context) throws InterruptedException {
        executeTestForTestID("1", context);
        Map<String, String> inputData = SyntheticDataUtil.getInputDataById(inputID);
        TestContextManager.setInputData(inputData);
        addProductsToCartAndPlaceOrder.deleteAddress();
        addProductsToCartAndPlaceOrder.addToCartAndGoToCart();
        addProductsToCartAndPlaceOrder.clickOnEstimateShippingButton();
        addProductsToCartAndPlaceOrder.clickTermsOfServiceButton();
        addProductsToCartAndPlaceOrder.clickCheckoutButton();
        addProductsToCartAndPlaceOrder.waitForCheckoutPageVisible();
        addProductsToCartAndPlaceOrder.fillBillingDetailsFromInput();
        addProductsToCartAndPlaceOrder.proceedThroughCheckout();
        addProductsToCartAndPlaceOrder.checkoutConfirmation();
        addProductsToCartAndPlaceOrder.verifyOrderSuccessMessage();
        orderInformationPage.clickOrderDetailsLink();
        // Capture and write order info to Excel
        int rowIndex = ExcelReaderUtil.findNextAvailableRow(
                ExcelReaderUtil.getSheet(
                        ConfigReader.getProperty("Test_Data_File_Path"),
                        ConfigReader.getProperty("Transactional_Data_Sheet_Name")
                )
        );
        context.setAttribute("ExcelRowIndex", rowIndex); // store in context
        orderInformationPage.saveDetailsToExcel(rowIndex);
    }
}
