package tests.mainpayform

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Apps
import io.qameta.allure.kotlin.AllureId
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Story
import io.qameta.allure.kotlin.junit4.AllureRunner
import mocks.models.PayMethod
import org.junit.Test
import screens.MainPayFormScreen
import mocks.models.PayMethod.*
import org.junit.runner.RunWith

@RunWith(AllureRunner::class)
@Epic("Automated")
@Story("Отображение платежной формы")
class MirPayTests : MainPayFormTests() {

    @Test
    @AllureId("2874829")
    fun mirPay() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, SBP, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(MIR_PAY)
                checkSecondaryPayButtons(TINKOFF_PAY, CARD)
            }
        }
    }

    @Test
    @AllureId("2875447")
    fun mirPay_2() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(MIR_PAY)
                checkSecondaryPayButtons(TINKOFF_PAY, CARD)
            }
        }
    }

    @Test
    @AllureId("2876733")
    fun mirPay_4() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY, MIR_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(MIR_PAY)
                checkSecondaryPayButtons(TINKOFF_PAY, CARD)
            }
        }
    }

    @Test
    @AllureId("2876888")
    fun mirPay_5() {
        run {
            prepareApp(addScheme = true, MIR_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(MIR_PAY)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2876979")
    fun mirPay_7() {
        run {
            prepareApp(addScheme = true, MIR_PAY, TINKOFF_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(MIR_PAY)
                checkSecondaryPayButtons(TINKOFF_PAY, CARD)
            }
        }
    }

    @Test
    @AllureId("2877419")
    fun mirPay_8() {
        run {
            prepareApp(addScheme = true, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(MIR_PAY)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2877468")
    fun mirPay_9() {
        run {
            prepareApp(addScheme = false, MIR_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(MIR_PAY)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2876443")
    fun mirPay_14() {
        run {
            prepareApp(addScheme = false, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(MIR_PAY)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    private fun TestContext<Unit>.prepareApp(addScheme: Boolean, vararg methods: PayMethod) {
        setGetTerminalPayMethodsMock(addScheme = addScheme, *methods)
        setupApps(Apps.MIR_PAY_APP)
        openPayForm()
    }
}