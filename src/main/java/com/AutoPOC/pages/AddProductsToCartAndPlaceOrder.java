package com.AutoPOC.pages;

import com.AutoPOC.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.List;
import java.util.Random;

public class AddProductsToCartAndPlaceOrder extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(AddProductsToCartAndPlaceOrder.class);

    @FindBy(xpath = "//input[@id='add-to-cart-button-5']")
    private WebElement addToCartButton;

    @FindBy(xpath = "//div[@class='item-box'][1]//img")
    private WebElement clickOnItem;

    @FindBy(xpath = "//li[@class='inactive']//a[normalize-space()='Apparel & Shoes']")
    private WebElement category;

    @FindBy(xpath = "//span[normalize-space()='Shopping cart']")
    private WebElement shoppingCartButton;

    @FindBy(xpath = "//select[@id='CountryId']")
    private WebElement countryDropdown;

    @FindBy(xpath = "//select[@id='StateProvinceId']")
    private WebElement stateDropdown;

    @FindBy(xpath = "//select[@id='StateProvinceId']/option")
    private List<WebElement> stateOptions;

    @FindBy(xpath = "//input[@name='estimateshipping']")
    private WebElement estimateShippingButton;

    @FindBy(xpath = "//input[@id='termsofservice']")
    private WebElement termsOfService;

    @FindBy(xpath = "//div[@class='checkout-buttons']")
    private WebElement checkoutButton;

    @FindBy(xpath = "//h1[text()='Checkout']")
    private WebElement checkoutHeader;

    @FindBy(xpath = "//input[@onclick='Billing.save()']")
    private WebElement billingContinueButton;

    @FindBy(xpath = "//input[@onclick='Shipping.save()']")
    private WebElement shippingContinueButton;

    @FindBy(xpath = "//input[@onclick='ShippingMethod.save()']")
    private WebElement shippingMethodContinueButton;

    @FindBy(xpath = "//input[@onclick='PaymentMethod.save()']")
    private WebElement paymentMethodContinueButton;

    @FindBy(xpath = "//input[@onclick='PaymentInfo.save()']")
    private WebElement paymentInfoContinueButton;

    @FindBy(xpath = "//input[@onclick='ConfirmOrder.save()']")
    private WebElement confirmOrderButton;

    @FindBy(xpath = "//strong[text()='Your order has been successfully processed!']")
    private WebElement successMessage;

    @FindBy(xpath = "//select[@id='shipping-address-select']")
    private WebElement shippingAddressSelect;

    /**
     * Clicks on Apparel & Shoes category
     */
    public void clickOnCategory() {
        waitForElementToBeClickable(category, 5);
        category.click();
        logger.info("Category is clicked");
    }

    /**
     * Clicks on a product and adds it to the cart
     */
    public void clickOnItemAndAddToCart() {
        click(clickOnItem);
        click(addToCartButton);
        logger.info("Item added to cart");
    }

    /**
     * Clicks on the Shopping Cart button
     */
    public void clickShoppingCartButton() {
        click(shoppingCartButton);
        logger.info("Shopping cart button is clicked");
    }

    /**
     * Selects United States from Country dropdown
     */
    public void selectUnitedStates() {
        waitForElementToBeClickable(countryDropdown, 5);
        new Select(countryDropdown).selectByVisibleText("United States");
        logger.info("United States is selected");
    }

    /**
     * Selects a random state from the dropdown
     */
    public void selectRandomState() {
        click(stateDropdown);
        Random random = new Random();
        int randomIndex = random.nextInt(stateOptions.size());
        new Select(stateDropdown).selectByIndex(randomIndex);

        if (!stateOptions.isEmpty()) {
            logger.info("Selected state: {}", stateOptions.get(randomIndex).getText());
        } else {
            logger.warn("State options list is empty! No state was selected.");
        }
    }

    /**
     * Clicks the Estimate Shipping button
     */
    public void clickOnEstimateShippingButton() {
        click(estimateShippingButton);
        logger.info("Estimate shipping button clicked");
    }

    /**
     * Clicks on the Terms of Service checkbox
     */
    public void clickTermsOfServiceButton() {
        click(termsOfService);
        logger.info("Terms of service accepted");
    }

    /**
     * Clicks the Checkout button
     */
    public void clickCheckoutButton() {
        click(checkoutButton);
        logger.info("Checkout button clicked");
    }

    /**
     * Waits for the Checkout page to be visible
     */
    public void waitForCheckoutPageVisible() {
        waitForElementToBeVisible(checkoutHeader, 7);
        logger.info("Checkout page is visible");
    }

    /**
     * Clicks Continue buttons in the checkout flow
     */
    public void clickOnContinueButtonsInCheckoutPage() {
        click(billingContinueButton);
        logger.info("Billing continue button clicked");
        click(shippingContinueButton);
        logger.info("Shipping continue button clicked");
        click(shippingMethodContinueButton);
        logger.info("Shipping method continue button clicked");
        click(paymentMethodContinueButton);
        logger.info("Payment method continue button clicked");
        click(paymentInfoContinueButton);
        logger.info("Payment info continue button clicked");
    }

    /**
     * Clicks the Confirm Order button
     */
    public void checkoutConfirmation() {
        click(confirmOrderButton);
        logger.info("Order confirmed");
    }

    /**
     * Verifies Order Success Message
     */
    public void verifyOrderSuccessMessage() {
        waitForTextToBePresent(successMessage, "Your order has been successfully processed!", 10);
        String actualMessage = successMessage.getText().trim();
        String expectedMessage = "Your order has been successfully processed!";
        Assert.assertEquals(actualMessage, expectedMessage, "Order success message mismatch!");
        logger.info("Order success message verified successfully.");
    }
}
