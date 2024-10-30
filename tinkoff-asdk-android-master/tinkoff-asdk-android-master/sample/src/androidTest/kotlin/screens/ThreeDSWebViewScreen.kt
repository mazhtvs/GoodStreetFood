package screens

import androidx.test.espresso.web.webdriver.Locator
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.web.KWebView
import io.qameta.allure.kotlin.Allure
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen

class ThreeDSWebViewScreen(testContext: TestContext<*>) : BaseScreen(testContext) {

    private val webView = KWebView { withId(R.id.acq_3ds_wv) }

    fun checkIsDisplayed() {
        step("Проверить отображение 3ds webview") {
            KView { withId(R.id.acq_3ds_wv) }.isDisplayed()
        }
    }

    fun sendOtp(otp: String) {
        step("Ввести OTP - $otp") {
            webView {
                withElement(Locator.NAME, "otp") {
                    click()
                    keys(otp)
                }
                withElement(Locator.ID, "sendButton") {
                    click()
                }
            }
        }
    }

    companion object {

        const val reportName: String = "3DS WebView"
        const val snapshotName: String = "3DS_WebView"

        inline operator fun invoke(
            testContext: TestContext<*>,
            crossinline block: ThreeDSWebViewScreen.() -> Unit,
        ): Unit =
            Allure.step(reportName) { ThreeDSWebViewScreen(testContext).block() }
    }

    override val reportName: String = Companion.reportName
    override val snapshotName: String = Companion.snapshotName
}