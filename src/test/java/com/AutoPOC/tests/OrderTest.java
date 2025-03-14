package com.AutoPOC.tests;

import com.AutoPOC.BaseTest;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OrderTest extends BaseTest {

    @Test(description = "login")
    public void login(ITestContext context) {
        executeTestForTestID("2", context);
    }

    @Test(description = "Add products to cart and place order")
    public void addProductsToCart(ITestContext context) {
        executeTestForTestID("2", context);
        addProductsToCartAndPlaceOrder.deleteAddress();
        addProductsToCartAndPlaceOrder.clickOnItemAndAddToCart();
        addProductsToCartAndPlaceOrder.clickShoppingCartButton();
        addProductsToCartAndPlaceOrder.selectUnitedStates();
        addProductsToCartAndPlaceOrder.selectRandomState();
        addProductsToCartAndPlaceOrder.clickOnEstimateShippingButton();
        addProductsToCartAndPlaceOrder.clickTermsOfServiceButton();
        addProductsToCartAndPlaceOrder.clickCheckoutButton();
        addProductsToCartAndPlaceOrder.waitForCheckoutPageVisible();
        addProductsToCartAndPlaceOrder.fillBillingDetails();
        addProductsToCartAndPlaceOrder.clickOnContinueButtonsInCheckoutPage();
        addProductsToCartAndPlaceOrder.checkoutConfirmation();
        addProductsToCartAndPlaceOrder.verifyOrderSuccessMessage();
        orderInformationPage.clickOrderDetailsLink();
        orderInformationPage.saveOrderDetailsToExcelFile();
    }
}
