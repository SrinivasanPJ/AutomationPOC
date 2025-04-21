package com.AutoPOC.pages;

import com.AutoPOC.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Page object for handling login functionality.
 * Includes actions for entering credentials, clicking login,
 * and validating login success.
 */
public class LoginPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);

    @FindBy(xpath = "//a[@class='ico-login']")
    private WebElement loginLink;

    @FindBy(xpath = "//input[@class='email']")
    private WebElement email;

    @FindBy(xpath = "//input[@class='password']")
    private WebElement password;

    @FindBy(xpath = "//input[@value='Log in']")
    private WebElement loginButton;

    @FindBy(xpath = "//a[@class='ico-logout']")
    private WebElement logoutLink;

    /**
     * Checks if the login was successful by verifying the presence of the logout link.
     *
     * @return {@code true} if the logout link is displayed, indicating a successful login;
     * {@code false} otherwise.
     */
    public boolean isLoginSuccessful() {
        try {
            return logoutLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Performs the login action by entering the provided credentials and clicking the login button.
     *
     * @param user the email or username to be used for login.
     * @param pass the password associated with the given user.
     * @throws AssertionError if the login attempt is not successful.
     */
    public void login(String user, String pass) {
        click(loginLink, "Main Login button clicked");
        logger.info("Attempting to login with email: {}", user);
        sendKeys(email, user);
        logger.info("Email entered");
        sendKeys(password, pass);
        logger.info("Password entered");
        click(loginButton, "Login button clicked");
        Assert.assertTrue(isLoginSuccessful(), "Login was not successful.");
    }
}
