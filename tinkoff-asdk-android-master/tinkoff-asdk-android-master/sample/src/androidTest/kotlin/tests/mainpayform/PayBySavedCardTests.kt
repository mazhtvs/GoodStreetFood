package tests.mainpayform

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Card
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
class PayBySavedCardTests : MainPayFormTests() {

    private val card: Card = CardFactory.generateRandomCard()

    @Test
    @AllureId("2874185")
    fun cardWithSavedCard() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, SBP, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2875439")
    fun cardWithSavedCard_2() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2872902")
    fun cardWithSavedCard_3() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2876731")
    fun cardWithSavedCard_4() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY, MIR_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2876886")
    fun cardWithSavedCard_5() {
        run {
            prepareApp(addScheme = true, MIR_PAY, SBP)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872904")
    fun cardWithSavedCard_6() {
        run {
            prepareApp(addScheme = true, TINKOFF_PAY)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2876975")
    fun cardWithSavedCard_7() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2877413")
    fun cardWithSavedCard_8() {
        run {
            prepareApp(addScheme = true, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2877462")
    fun cardWithSavedCard_9() {
        run {
            prepareApp(addScheme = false, SBP, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872914")
    fun cardWithSavedCard_10() {
        run {
            prepareApp(addScheme = false, SBP, TINKOFF_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2872892")
    fun cardWithSavedCard_11() {
        run {
            prepareApp(addScheme = true, SBP)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872907")
    fun cardWithSavedCard_12() {
        run {
            prepareApp(addScheme = false, TINKOFF_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayButtons(TINKOFF_PAY)
            }
        }
    }

    @Test
    @AllureId("2872897")
    fun cardWithSavedCard_13() {
        run {
            prepareApp(addScheme = true)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2876441")
    fun cardWithSavedCard_14() {
        run {
            prepareApp(addScheme = false, MIR_PAY)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872915")
    fun cardWithSavedCard_15() {
        run {
            prepareApp(addScheme = false, SBP)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872906")
    fun cardWithSavedCard_16() {
        run {
            prepareApp(addScheme = false)

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    private fun TestContext<Unit>.prepareApp(addScheme: Boolean, vararg methods: PayMethod) {
        setGetTerminalPayMethodsMock(addScheme, *methods)
        stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.withCard(card))
        openPayForm()
    }
}