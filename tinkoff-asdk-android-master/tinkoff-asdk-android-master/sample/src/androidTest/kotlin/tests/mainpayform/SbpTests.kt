package tests.mainpayform

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Apps
import dataproviders.card.CardFactory
import io.qameta.allure.kotlin.AllureId
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Story
import io.qameta.allure.kotlin.junit4.AllureRunner
import mocks.GetCardListMock
import mocks.models.PayMethod
import org.junit.Test
import screens.MainPayFormScreen
import mocks.models.PayMethod.*
import org.junit.runner.RunWith
import ru.tinkoff.core.testing.mock.stubs_factory.AllureStepMock.stubFor

@RunWith(AllureRunner::class)
@Epic("Automated")
@Story("Отображение платежной формы")
class SbpTests : MainPayFormTests() {

    @Test
    @AllureId("2874830")
    fun sbp() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true, TINKOFF_PAY, SBP, MIR_PAY)
            setupApps(Apps.MIR_PAY_APP, Apps.SBP_APP)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(
                    TINKOFF_PAY,
                    MIR_PAY,
                    CARD
                )
            }
        }
    }

    @Test
    @AllureId("2875446")
    fun sbp_2() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(
                    TINKOFF_PAY
                )
            }
        }
    }

    @Test
    @AllureId("2872893")
    fun sbp_3() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(
                    TINKOFF_PAY,
                    CARD
                )
            }
        }
    }

    @Test
    @AllureId("2876734")
    fun sbp_4() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY, SBP, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(
                    TINKOFF_PAY,
                    CARD
                )
            }
        }
    }

    @Test
    @AllureId("2876889")
    fun sbp_5() {
        run {
            prepareApp(addScheme = true, SBP, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2872913")
    fun sbp_6() {
        run {
            val card = CardFactory.generateRandomCard()
            stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.withCard(card))
            prepareApp(addScheme = true, TINKOFF_PAY)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2876980")
    fun sbp_7() {
        run {
            val card = CardFactory.generateRandomCard()
            stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.withCard(card))
            prepareApp(addScheme = false, TINKOFF_PAY, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2877414")
    fun sbp_8() {
        run {
            val card = CardFactory.generateRandomCard()
            stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.withCard(card))
            prepareApp(addScheme = true, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2877469")
    fun sbp_9() {
        run {
            prepareApp(addScheme = false, MIR_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2872898")
    fun sbp_10() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(
                    TINKOFF_PAY,
                    CARD
                )
            }
        }
    }

    @Test
    @AllureId("2872887")
    fun sbp_11() {
        run {
            prepareApp(addScheme = true, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2872911")
    fun sbp_12() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2872912")
    fun sbp_13() {
        run {
            prepareApp(addScheme = true)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2876511")
    fun sbp_14() {
        run {
            prepareApp(addScheme = false, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872894")
    fun sbp_15() {
        run {
            prepareApp(addScheme = false, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    private fun TestContext<Unit>.prepareApp(addScheme: Boolean, vararg methods: PayMethod) {
        setGetTerminalPayMethodsMock(addScheme, *methods)
        setupApps(Apps.SBP_APP)
        openPayForm()
    }
}