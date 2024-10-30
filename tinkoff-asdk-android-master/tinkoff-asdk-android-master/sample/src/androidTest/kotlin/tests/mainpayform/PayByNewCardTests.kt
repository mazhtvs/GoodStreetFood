package tests.mainpayform

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Apps
import io.qameta.allure.kotlin.AllureId
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Story
import io.qameta.allure.kotlin.junit4.AllureRunner
import mocks.GetCardListMock
import mocks.models.*
import mocks.models.PayMethod.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.tinkoff.core.testing.mock.stubs_factory.AllureStepMock.stubFor
import screens.MainPayFormScreen

@RunWith(AllureRunner::class)
@Epic("Automated")
@Story("Отображение платежной формы")
class PayByNewCardTests : MainPayFormTests() {

    @Test
    @AllureId("2873933")
    fun cardWithoutSavedCard() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, SBP, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2874831")
    fun cardWithoutSavedCard_2() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2872895")
    fun cardWithoutSavedCard_3() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2876730")
    fun cardWithoutSavedCard_4() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY, MIR_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2876885")
    fun cardWithoutSavedCard_5() {
        run {
            prepareApp(addScheme = true, MIR_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872905")
    fun cardWithoutSavedCard_6() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2876974")
    fun cardWithoutSavedCard_7() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2877408")
    fun cardWithoutSavedCard_8() {
        run {
            prepareApp(addScheme = true, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2877466")
    fun cardWithoutSavedCard_9() {
        run {
            prepareApp(addScheme = false, MIR_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872900")
    fun cardWithoutSavedCard_10() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2872901")
    fun cardWithoutSavedCard_11() {
        run {
            prepareApp(addScheme = true, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872890")
    fun cardWithoutSavedCard_12() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2872917")
    fun cardWithoutSavedCard_13() {
        run {
            prepareApp(addScheme = true)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2876440")
    fun cardWithoutSavedCard_14() {
        run {
            prepareApp(addScheme = false, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872899")
    fun cardWithoutSavedCard_15() {
        run {
            prepareApp(addScheme = false, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872889")
    fun cardWithoutSavedCard_16() {
        run {
            prepareApp(addScheme = false)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872910")
    fun tinkoffPayAndSbpInstalled() {
        run {
            setupApps(Apps.TINKOFF, Apps.SBP_APP)
            prepareApp(addScheme = false)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    private fun TestContext<Unit>.prepareApp(addScheme: Boolean, vararg methods: PayMethod) {
        setGetTerminalPayMethodsMock(addScheme = addScheme, *methods)
        stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.successNoActiveCard())
        openPayForm()
    }
}