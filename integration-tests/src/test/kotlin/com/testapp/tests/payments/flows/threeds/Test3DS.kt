package com.testapp.tests.payments.flows.threeds

import com.testapp.base.BaseAppiumTest
import com.testapp.base.PaymentCategory
import com.testapp.base.RetryRule
import com.testapp.network.KlarnaApi
import com.testapp.tests.payments.flows.paynow.TestSliceItUK
import com.testapp.utils.*
import io.appium.java_client.android.AndroidDriver
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions

internal class Test3DS : BaseAppiumTest() {
    companion object {

        @JvmStatic
        @BeforeClass
        fun setup() {
            testName = TestSliceItUK::class.java.simpleName
            BaseAppiumTest.setup()
        }
    }

    @Rule
    @JvmField
    var retryRule = RetryRule(retryCount, ignoreOnFailure)

    @Test
    fun `test 3ds successful flow`() {
        test3ds(true)
    }

    @Test
    fun `test 3ds failure flow`() {
        test3ds(false)
    }


    fun test3ds(success: Boolean) {
        val session = KlarnaApi.getSessionInfo(SessionHelper.getRequestDE())?.session
        if (session?.client_token == null || !session.payment_method_categories.map { it.identifier }.contains(PaymentCategory.SLICE_IT.value)) {
            return
        }
        val token = session.client_token
        initLoadSDK(token, PaymentCategory.PAY_NOW.value)
        DriverUtils.switchContextToWebView(driver)

        val mainWindow = WebViewTestHelper.findWindowFor(driver, By.id("klarna-some-hardcoded-instance-id-main"))
        mainWindow?.let {
            driver.switchTo().window(it)
        } ?: Assert.fail("Main window wasn't found")
        DriverUtils.getWaiter(driver).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("klarna-some-hardcoded-instance-id-main"))
        DriverUtils.getWaiter(driver).until(ExpectedConditions.presenceOfElementLocated(By.id("installments-card|-1"))).click()
        DriverUtils.getWaiter(driver).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.xpath("//*[@id=\"pay-now-card\"]/iframe")))
        PaymentFlowsTestHelper.fillCardInfo(driver, true)
        // switch to native context
        DriverUtils.switchContextToNative(driver)
        driver.hideKeyboard()
        PaymentFlowsTestHelper.dismissConsole()
        try {
            driver.findElement(ByRnId(driver, "authorizeButton_${PaymentCategory.PAY_NOW.value}")).click()
        } catch (t: Throwable) {
            driver.findElementByAndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(new UiSelector().description(\"authorizeButton_${PaymentCategory.PAY_NOW.value}\"))")
            DriverUtils.getWaiter(driver).until(ExpectedConditions.presenceOfElementLocated(ByRnId(driver, "authorizeButton_${PaymentCategory.PAY_NOW.value}"))).click()
        }
        // enter billing address
        val billing = BillingAddressTestHelper.getBillingInfoDE()
        PaymentFlowsTestHelper.fillBillingAddress(driver, billing)

        val window = WebViewTestHelper.findWindowFor(driver, By.id("klarna-some-hardcoded-instance-id-fullscreen"))
        window?.let {
            driver.switchTo().window(it)
        } ?: Assert.fail("3DS window wasn't found")
        DriverUtils.getWaiter(driver).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("klarna-some-hardcoded-instance-id-fullscreen"))
        val frame = DriverUtils.waitForPresence(driver, By.id("3ds-dialog-iframe"))
        driver.switchTo().frame(frame)
        val actionSelector = if (success) By.id("success") else By.id("rejected")
        DriverUtils.getWaiter(driver).until(ExpectedConditions.and(ExpectedConditions.visibilityOfElementLocated(actionSelector), ExpectedConditions.elementToBeClickable(actionSelector)))

        var retryCount = 0
        var retries = 5
        while (retryCount < retries) {
            try {
                DriverUtils.getWaiter(driver).until(ExpectedConditions.presenceOfElementLocated(actionSelector)).click()
                break
            } catch (t: Throwable) {
                if (retryCount < retries - 1) {
                    DriverUtils.wait(driver, 1)
                    retryCount++
                } else {
                    throw t
                }
            }
        }

        if (success) {
            DriverUtils.switchContextToNative(driver)
            var response = PaymentFlowsTestHelper.readConsoleMessage(driver, "authToken")?.text
            PaymentFlowsTestHelper.checkAuthorizeResponse(response, true)
        } else {
            DriverUtils.getWaiter(driver).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("klarna-some-hardcoded-instance-id-fullscreen"))
            val refusedTextBy = By.xpath("//*[@id=\"message-component-root\"]")
            val refusedText =
                    DriverUtils.getWaiter(driver).until(ExpectedConditions.presenceOfElementLocated(refusedTextBy))
            with(refusedText.text.toLowerCase()) {
                assert(this.contains("sorry") || this.contains("unfortunately"))
            }
        }
    }
}