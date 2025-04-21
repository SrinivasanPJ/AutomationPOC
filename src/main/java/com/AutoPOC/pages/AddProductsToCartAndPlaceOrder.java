package com.AutoPOC.pages;

import com.AutoPOC.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Page object that encapsulates all actions related to
 * adding products to cart and completing the order process.
 */
public class AddProductsToCartAndPlaceOrder extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(AddProductsToCartAndPlaceOrder.class);

    @FindBy(css = "div.header-links a.account")
    private WebElement accountLink;

    @FindBy(xpath = "//a[contains(@class, 'active') or contains(@class, 'inactive')][normalize-space()='Addresses']")
    private WebElement addressesLink;

    @FindBy(xpath = "//input[@value='Delete']")
    private WebElement deleteAddressButton;

    @FindBy(xpath = "//input[@name='estimateshipping']")
    private WebElement estimateShippingButton;

    @FindBy(xpath = "//input[@id='termsofservice']")
    private WebElement termsOfService;

    @FindBy(xpath = "//input[starts-with(@onclick,'Billing.save')"
            + " or starts-with(@onclick,'Shipping.save')"
            + " or starts-with(@onclick,'ShippingMethod.save')"
            + " or starts-with(@onclick,'PaymentMethod.save')"
            + " or starts-with(@onclick,'PaymentInfo.save')"
            + " or starts-with(@onclick,'ConfirmOrder.save')]")
    private List<WebElement> continueButtons;

    @FindBy(xpath = "//strong[text()='Your order has been successfully processed!']")
    private WebElement successMessage;

    @FindBy(xpath = "//input[starts-with(@id, 'add-to-cart-button')]")
    private WebElement addToCartButton;

    @FindBy(xpath = "//span[normalize-space()='Shopping cart']")
    private WebElement shoppingCartButton;

    @FindBy(xpath = "//select[@id='BillingNewAddress_CountryId']")
    private WebElement countryDropdown;

    @FindBy(xpath = "//select[@id='BillingNewAddress_StateProvinceId']")
    private WebElement stateDropdown;

    @FindBy(xpath = "//div[@class='checkout-buttons']")
    private WebElement checkoutButton;

    @FindBy(xpath = "//h1[text()='Checkout']")
    private WebElement checkoutHeader;

    @FindBy(id = "BillingNewAddress_FirstName")
    private WebElement billingFirstName;

    @FindBy(id = "BillingNewAddress_LastName")
    private WebElement billingLastName;

    @FindBy(id = "BillingNewAddress_Email")
    private WebElement billingEmail;

    @FindBy(id = "BillingNewAddress_City")
    private WebElement billingCity;

    @FindBy(id = "BillingNewAddress_Address1")
    private WebElement billingAddress1;

    @FindBy(id = "BillingNewAddress_ZipPostalCode")
    private WebElement billingZipPostalCode;

    @FindBy(id = "BillingNewAddress_PhoneNumber")
    private WebElement billingPhoneNumber;

    @FindBy(xpath = "//input[contains(@class,'confirm-order-next-step-button') and @value='Confirm']")
    private WebElement confirmOrderButton;

    // ─── Workflow Methods ───────────────────────────────────────────────

    public void deleteAddress() throws InterruptedException {
        click(accountLink, "Clicked Account link");
        click(addressesLink, "Clicked Addresses link");

        if (isDisplayed(deleteAddressButton, 5)) {
            deleteAddressButton.click();
            logger.info("Clicked delete button");

            Alert alert = driver.switchTo().alert();
            logger.info("Alert displayed: {}", alert.getText());
            alert.accept();
            logger.info("Alert accepted");

            driver.navigate().refresh();
            logger.info("Page refreshed using navigate().refresh()");

            Thread.sleep(2000);
            boolean deleted = waitUntilElementGone(deleteAddressButton);
            logger.info(deleted ? "Address deleted successfully." : "Delete button still visible or address block not cleared.");
        }

        if (getNumberOfAddresses() == 0) {
            logger.info("No address found. Proceeding to product selection.");
            selectProductBasedOnInputData();
        } else {
            logger.info("Address still present, skipping product selection.");
        }
    }

    public void clickTermsOfServiceButton() {
        click(termsOfService, "Terms of service accepted");
    }

    public void checkoutConfirmation() {
        try {
            confirmOrderButton.click();
            logger.info("Confirm order clicked immediately without wait");
        } catch (Exception e) {
            logger.warn("Immediate click failed, retrying with wait...");
            waitUntilVisible(confirmOrderButton, 5);
            waitUntilClickable(confirmOrderButton, 5);
            confirmOrderButton.click();
        }
    }

    public void selectProductBasedOnInputData() {
        Map<String, String> data = getInputData();
        clickBy("Category", data.get("Category"), "//ul[@class='top-menu']//a[normalize-space()='%s']");
        clickBy("Sub-Category", data.get("Sub-Category"), "//div[@class='sub-category-item']//a[normalize-space()='%s']");
        clickBy("Product title", data.get("Product title"), "//h2[@class='product-title']/a[contains(text(),'%s')]");
    }

    public void waitForCheckoutPageVisible() {
        waitUntilVisible(checkoutHeader, 7);
        logger.info("Checkout page is visible");
    }

    public void addToCartAndGoToCart() {
        click(addToCartButton, "Add to Cart clicked");
        click(shoppingCartButton, "Shopping Cart clicked");
    }

    public void clickOnEstimateShippingButton() {
        click(estimateShippingButton, "Estimate shipping button clicked");
    }

    public void fillBillingDetailsFromInput() throws InterruptedException {
        Map<String, String> d = getInputData();
        logger.info("---- synthetic inputData keys&values ----");
        d.forEach((k, v) -> logger.info("[{}] → [{}]", k, v));

        sendKeys(billingFirstName, d.get("Billing FirstName"));
        sendKeys(billingLastName, d.get("Billing LastName"));
        sendKeys(billingEmail, d.get("Email"));

        selectByVisibleText(countryDropdown, d.get("Country"));
        selectStateOption(d.get("State"));

        sendKeys(billingCity, d.get("City"));
        sendKeys(billingAddress1, d.get("Address 1"));
        sendKeys(billingZipPostalCode, d.get("Zip"));
        sendKeys(billingPhoneNumber, d.get("Phone"));

        logger.info("Filled billing & shipping details");
    }

    public void proceedThroughCheckout() {
        for (WebElement btn : continueButtons) {
            click(btn, "Clicked continue button");
        }
    }

    public void clickCheckoutButton() {
        click(checkoutButton, "Checkout button clicked");
    }

    public void verifyOrderSuccessMessage() {
        waitUntilVisible(successMessage, 10);
        waitUntilTextPresent(successMessage, "Your order has been successfully processed!", 10);
        logger.info("Order success message verified");
    }

    // ─── Helper Methods ────────────────────────────────────────────────

    private void selectStateOption(String state) throws InterruptedException {
        var select = new Select(stateDropdown);
        Thread.sleep(3000);
        var opts = select.getOptions();
        if (state != null && !state.isBlank()) {
            select.selectByVisibleText(state);
            logger.info("Explicitly selected state: {}", state);
        } else if (!opts.isEmpty()) {
            String random = opts.get(new Random().nextInt(opts.size())).getText();
            select.selectByVisibleText(random);
            logger.info("Random state selected: {}", random);
        }
    }
}
