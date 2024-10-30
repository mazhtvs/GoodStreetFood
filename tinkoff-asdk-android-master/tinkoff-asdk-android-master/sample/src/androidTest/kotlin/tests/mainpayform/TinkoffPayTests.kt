package tests.mainpayform

import dataproviders.card.Apps.*
import io.qameta.allure.kotlin.AllureId
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Story
import io.qameta.allure.kotlin.junit4.AllureRunner
import mocks.models.PayMethod.*
import org.junit.Test
import org.junit.runner.RunWith
import screens.MainPayFormScreen

@RunWith(AllureRunner::class)
@Epic("Automated")
@Story("Отображение платежной формы")
class TinkoffPayTests : MainPayFormTests() {

    @Test
    @AllureId("2874749")
    fun tinkoffPay() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true, TINKOFF_PAY, SBP, MIR_PAY)
            setupApps(TINKOFF, SBP_APP, MIR_PAY_APP)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                checkSecondaryPayButtons(
                    SBP,
                    MIR_PAY,
                    CARD
                )
            }
        }
    }

    @Test
    @AllureId("2875448")
    fun tinkoffPay_2() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true, TINKOFF_PAY, MIR_PAY)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    /**
     * 3. Доступны: TinkoffPay, MirPay, Сохранение карт
     */
    @Test
    @AllureId("2872891")
    fun tinkoffPay_3() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true, TINKOFF_PAY, SBP)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                checkSecondaryPayButtons(
                    SBP,
                    CARD
                )
            }
        }
    }

    /**
     * 4. Доступны: TinkoffPay, СБП, МirPay
     */
    @Test
    @AllureId("2876732")
    fun tinkoffPay_4() {
        run {
            setGetTerminalPayMethodsMock(addScheme = false, TINKOFF_PAY, SBP, MIR_PAY)
            setupApps(TINKOFF, SBP_APP, MIR_PAY_APP)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                checkSecondaryPayButtons(
                    SBP,
                    MIR_PAY,
                    CARD
                )
            }
        }
    }

    @Test
    @AllureId("2876891")
    fun tinkoffPay_5() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true, SBP, MIR_PAY)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872896")
    fun tinkoffPay_6() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true, TINKOFF_PAY)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2876978")
    fun tinkoffPay_7() {
        run {
            setGetTerminalPayMethodsMock(addScheme = false, TINKOFF_PAY, MIR_PAY)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2877416")
    fun tinkoffPay_8() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true, MIR_PAY)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2877470")
    fun tinkoffPay_9() {
        run {
            setGetTerminalPayMethodsMock(addScheme = false, MIR_PAY, SBP)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2872886")
    fun tinkoffPay_10() {
        run {
            setGetTerminalPayMethodsMock(addScheme = false, SBP, TINKOFF_PAY)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                checkSecondaryPayButtons(
                    SBP,
                    CARD
                )
            }
        }
    }

    @Test
    @AllureId("2872909")
    fun tinkoffPay_11() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true, SBP)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2872888")
    fun tinkoffPay_12() {
        run {
            setGetTerminalPayMethodsMock(addScheme = false, TINKOFF_PAY)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

    @Test
    @AllureId("2872916")
    fun tinkoffPay_13() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2876444")
    fun tinkoffPay_14() {
        run {
            setGetTerminalPayMethodsMock(addScheme = false, MIR_PAY)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(CARD)
                checkSecondaryPayMethodsIsNotDisplayed()
            }
        }
    }

    @Test
    @AllureId("2872908")
    fun tinkoffPay_15() {
        run {
            setGetTerminalPayMethodsMock(addScheme = false, SBP)
            setupApps(TINKOFF)

            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(SBP)
                checkSecondaryPayButtons(CARD)
            }
        }
    }

}