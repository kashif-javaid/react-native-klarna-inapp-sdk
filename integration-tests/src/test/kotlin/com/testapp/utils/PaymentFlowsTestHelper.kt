package com.testapp.utils

import com.testapp.base.BaseAppiumTest
import com.testapp.constants.AppiumTestConstants
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.json.JSONObject
import org.junit.Assert
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions

internal object PaymentFlowsTestHelper {

    fun fillBillingAddress(driver: AndroidDriver<MobileElement>, billingInfo: LinkedHashMap<String, String?>) {
        val loadingLocator = By.id("loading-overlay")

        // switch to klarna payment billing address iframe
        val billingWindow =
                WebViewTestHelper.findWindowFor(driver, By.id("klarna-some-hardcoded-instance-id-fullscreen"))
        billingWindow?.let {
            driver.switchTo().window(it)
        } ?: Assert.fail("Billing address window wasn't found")
        DriverUtils.getWaiter(driver).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("klarna-some-hardcoded-instance-id-fullscreen"))

        val selectAllKeys = Keys.chord(Keys.CONTROL, "a")
        for ((key, value) in billingInfo) {
            value?.let {
                val element = DriverUtils.getWaiter(driver).until(ExpectedConditions.presenceOfElementLocated(By.xpath(key)))
                element.apply {
                    if (isEnabled) {
                        sendKeys(selectAllKeys)
                        sendKeys(Keys.DELETE)
                        DriverUtils.getWaiter(driver)
                                .until(ExpectedConditions.invisibilityOfElementLocated(loadingLocator))
                        sendKeys(value)
                    }
                }
            }
        }

        // click on the submit button
        val submitButtonBy = By.id("identification-dialog__footer-button-wrapper")
        DriverUtils.getWaiter(driver).until(ExpectedConditions.presenceOfElementLocated(submitButtonBy)).click()

        // check if we should continue anyway
        try {
            DriverUtils.wait(driver, 1)
            val continueAnyWay = driver.findElement(submitButtonBy)
            if(continueAnyWay.text == "Continue anyway"){
                continueAnyWay.click()
            }
        } catch (exception: Exception){
            // no warning
        }

        DriverUtils.wait(driver, 2)

        // switch to confirmation window
        val confirmationWindow =
                WebViewTestHelper.findWindowFor(driver, By.id("klarna-some-hardcoded-instance-id-fullscreen"))
        confirmationWindow?.let {
            driver.switchTo().window(it)
        } ?: Assert.fail("Confirmation window wasn't found")
        DriverUtils.getWaiter(driver).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("klarna-some-hardcoded-instance-id-fullscreen"))

        // confirm the billing info
        DriverUtils.getWaiter(driver).until(ExpectedConditions.presenceOfElementLocated(submitButtonBy)).click()
    }

    fun readConsoleMessage(driver: AndroidDriver<MobileElement>, containText: String): WebElement?{
        return DriverUtils.getWaiter(driver).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//android.widget.TextView[contains(@text, '$containText')]")))
    }

    fun checkAuthorizeResponse(response: String?, successful: Boolean) {
        assert(!response.isNullOrBlank())
        val json = JSONObject(response!!.substring(response.indexOf("{")))
        if(successful){
            assert(json.getBoolean("approved"))
            try{
                json.getString("authToken").toLowerCase().apply {
                    assert(!this.isBlank() && this != "null")
                }
            } catch (exception: Exception){
                Assert.fail("Null authorization token.")
            }
        }
        else {
            Assert.assertFalse(json.getBoolean("approved"))
        }
    }

    fun fillCardInfo(driver: AndroidDriver<MobileElement>, is3ds: Boolean = false) {
        DriverUtils.getWaiter(driver).until(ExpectedConditions.presenceOfElementLocated(By.id("cardNumber")))
        driver.findElementById("cardNumber").sendKeys(if(is3ds) AppiumTestConstants.CARD_NUMBER_3DS else AppiumTestConstants.CARD_NUMBER)
        driver.findElementById("expire").sendKeys(AppiumTestConstants.CARD_EXPIREDATE)
        driver.findElementById("securityCode").sendKeys(AppiumTestConstants.CARD_CVV)
    }

    fun dismissConsole() {
        try {
            readConsoleMessage(BaseAppiumTest.driver, "Dismiss All")?.click()
        } catch (t: Throwable){}
    }
}