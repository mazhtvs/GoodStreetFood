package tests

import dataproviders.card.Apps
import io.qameta.allure.kotlin.AllureId
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Story
import io.qameta.allure.kotlin.junit4.AllureRunner
import mocks.GetCardListMock
import mocks.GetStateMock
import mocks.InitMock
import mocks.TinkoffPayLinkMock
import mocks.models.PayMethod.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus.*
import ru.tinkoff.core.testing.mock.stubs_factory.AllureStepMock.stubFor
import ru.tinkoff.core.testing.mock.utils.WireMockAssertions.verifyRequest
import screens.ChromeBrowserScreen
import screens.MainPayFormScreen
import screens.PaymentStatusSheet
import screens.SampleMainScreen
import screens.SampleProductDetailScreen
import tests.mainpayform.MainPayFormTests

@RunWith(AllureRunner::class)
@Epic("Automated")
@Story("Оплата TinkoffPay")
class PayByTinkoffPayTests : MainPayFormTests() {

    @Test
    @AllureId("2519533")
    fun successPayment() {
        run {
            setupStubs()
            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                clickPrimaryPayButton()
            }

            ChromeBrowserScreen(this) {
                checkIsOpened()
                pressBack()
            }

            PaymentStatusSheet(this) {
                checkSheetContents(
                    logoIsVisible = false,
                    loaderIsVisible = true,
                    titleText = "Ждем оплату в приложении банка",
                    secondaryButtonText = "Закрыть"
                )
            }

            stubFor(GetStateMock.matcher, willReturn = GetStateMock.success(status = AUTHORIZED))

            PaymentStatusSheet(this) {
                checkSheetContents(titleText = "Оплачено", mainButtonText = "Понятно")
                mainButtonClick()
            }
        }
    }

    @Test
    @AllureId("2519530")
    fun openWebViewIfBankAppIsNotInstalled() {
        run {
            setGetTerminalPayMethodsMock(addScheme = true, TINKOFF_PAY)
            stubFor(GetCardListMock.matcher, GetCardListMock.successNoActiveCard())

            openPayForm()

            MainPayFormScreen(this) {
                checkSecondaryPayButtons(TINKOFF_PAY)
                clickOnSecondaryPayMethod(TINKOFF_PAY)
            }

            ChromeBrowserScreen(this) {
                checkIsOpened()
                pressBack()
            }

            PaymentStatusSheet(this) {
                checkIsNotDisplayed()
            }

            verifyRequest(InitMock.requestPattern, count = 0)
        }
    }

    @Test
    @AllureId("2925144")
    fun failedPayment() {
        run {
            setupStubs()
            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                clickPrimaryPayButton()
            }

            ChromeBrowserScreen(this) {
                checkIsOpened()
                pressBack()
            }

            stubFor(GetStateMock.matcher, willReturn = GetStateMock.success(status = REJECTED))

            PaymentStatusSheet(this) {
                checkSheetContents(
                    logoIsVisible = true,
                    titleText = "Не получилось оплатить",
                    subtitleText = "Выберите другой способ оплаты",
                    mainButtonText = "Выбрать другой способ",
                )
                mainButtonClick()
            }

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                clickPrimaryPayButton()
            }
        }
    }

    @Test
    @AllureId("2519517")
    fun paymentTimeHasExpired() {
        run {
            setupStubs()
            openPayForm()

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
                clickPrimaryPayButton()
            }

            ChromeBrowserScreen(this) {
                checkIsOpened()
                pressBack()
            }

            stubFor(GetStateMock.matcher, willReturn = GetStateMock.success(status = AUTHORIZING))

            PaymentStatusSheet(this) {
                checkSheetContents(
                    logoIsVisible = false,
                    loaderIsVisible = true,
                    titleText = "Ждем оплату в приложении банка",
                    secondaryButtonText = "Закрыть"
                )
            }

            PaymentStatusSheet(this) {
                checkSheetContents(
                    logoIsVisible = true,
                    titleText = "Время оплаты истекло",
                    subtitleText = "Попробуйте оплатить снова или выберите другой способ оплаты",
                    mainButtonText = "Другой способ оплаты",
                    timeout = ONE_MINUTE
                )
                mainButtonClick()
            }

            MainPayFormScreen(this) {
                checkPrimaryButton(TINKOFF_PAY)
            }

            verifyRequest(GetStateMock.requestPattern, count = 10)
        }
    }

    @Test
    @AllureId("3744958")
    fun paymentViaButtonFromMerchantApp() {
        run {
            setupStubs()

            SampleMainScreen(this) {
                clickOnDetailButton()
            }

            SampleProductDetailScreen(this) {
                clickOnTinkoffPayButton()
            }

            ChromeBrowserScreen(this) {
                checkIsOpened()
                pressBack()
            }

            PaymentStatusSheet(this) {
                checkSheetContents(
                    logoIsVisible = false,
                    loaderIsVisible = true,
                    titleText = "Ждем оплату в приложении банка",
                    secondaryButtonText = "Закрыть"
                )
            }

            stubFor(GetStateMock.matcher, willReturn = GetStateMock.success(status = AUTHORIZED))

            PaymentStatusSheet(this) {
                checkSheetContents(titleText = "Оплачено", mainButtonText = "Понятно")
                mainButtonClick()
            }
        }
    }

    private fun setupStubs() {
        setGetTerminalPayMethodsMock(addScheme = true, TINKOFF_PAY)
        setupApps(Apps.TINKOFF)
        stubFor(InitMock.matcher, InitMock.success())
        stubFor(GetCardListMock.matcher, GetCardListMock.successNoActiveCard())
        stubFor(TinkoffPayLinkMock.matcher, TinkoffPayLinkMock.success())
        stubFor(GetStateMock.matcher, GetStateMock.success(status = FORM_SHOWED))
    }
}