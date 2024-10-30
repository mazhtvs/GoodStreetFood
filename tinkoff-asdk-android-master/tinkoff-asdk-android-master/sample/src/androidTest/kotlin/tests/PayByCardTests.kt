package tests

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Card
import dataproviders.card.CardFactory
import io.github.kakaocup.kakao.screen.Screen
import io.qameta.allure.kotlin.AllureId
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Story
import io.qameta.allure.kotlin.junit4.AllureRunner
import mocks.Check3dsVersionMock
import mocks.FinishAuthorizeMock
import mocks.GetCardListMock
import mocks.GetStateMock
import mocks.GetTerminalPayMethodsMock
import mocks.InitMock
import mocks.Submit3DSAuthorizationV2Mock
import mocks.models.PayMethod
import mocks.threeds.ChallengeCompleteMock
import mocks.threeds.ChallengeMock
import mocks.threeds.Complete3DSMethodv2Mock
import org.junit.Test
import org.junit.runner.RunWith
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.core.testing.mock.stubs_factory.AllureStepMock.stubFor
import screens.PaymentStatusSheet
import screens.MainPayFormScreen
import screens.PayNewCardScreen
import screens.SampleProductDetailScreen
import screens.ThreeDSWebViewScreen
import tests.mainpayform.MainPayFormTests

@RunWith(AllureRunner::class)
@Epic("Automated")
@Story("Оплата картой")
class PayByCardTests : MainPayFormTests() {
    @Test
    @AllureId("3656518")
    fun payByNewNO3DSCard() = payByNewCard(CheckType.NO)

    @Test
    @AllureId("3656519")
    fun payByNew3DSCard() = payByNewCard(CheckType.THREE_DS)

    @Test
    @AllureId("3757855")
    fun payBySavedNO3DSCard() = payBySavedCard(CheckType.NO)

    @Test
    @AllureId("3757856")
    fun payBySaved3DSCard() = payBySavedCard(CheckType.THREE_DS)

    private fun payBySavedCard(checkType: CheckType) {
        run {
            pay(
                checkType,
                additionalStub = { card ->
                    setGetTerminalPayMethodsMock(addScheme = true, PayMethod.TINKOFF_PAY)
                    stubFor(GetCardListMock.matcher, GetCardListMock.successNoActiveCard())
                    stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.withCard(card))
                },
                initPay = { card ->
                    MainPayFormScreen(this) {
                        checkPrimaryForSavedCard(card, "Оплатить 20,99 ₽")
                        typeCvc("111")
                        clickOnLoaderButton()
                    }
                })
        }
    }

    private fun payByNewCard(checkType: CheckType) {
        run {
            pay(
                checkType,
                additionalStub = {},
                initPay = { card ->
                    MainPayFormScreen(this) {
                        checkPrimaryPayButtonIsEnabled()
                        clickPrimaryPayButton()
                    }

                    PayNewCardScreen(this) {
                        checkScreenTitle()
                        checkPayByNewCardButtonIsDisable()
                        cardInputFields.checkCardNumberField()
                        cardInputFields.checkExpireField()
                        cardInputFields.checkCVCField()

                        cardInputFields.enterCardData(card)
                        checkPayByNewCardButtonIsEnabled()
                        clickPayByNewCardButton()
                    }
                })
        }
    }

    private inline fun pay(
        checkType: CheckType,
        crossinline additionalStub: (card: Card) -> Unit,
        crossinline initPay: (card: Card) -> Unit
    ) {
        run {
            val card = CardFactory.generateRandomCard()
            setStubs(checkType = checkType)
            additionalStub(card)

            openPayForm()
            initPay(card)
            finishPay(checkType)
        }
    }

    private fun TestContext<Unit>.finishPay(checkType: CheckType) {
        when (checkType) {
            CheckType.THREE_DS -> {
                ThreeDSWebViewScreen(this) {
                    checkIsDisplayed()
                    sendOtp("1111")
                }
            }

            else -> {}
        }

        PaymentStatusSheet(this) {
            checkSheetContents(titleText = "Оплачено", mainButtonText = "Понятно")
            Screen.idle()
            mainButtonClick()
        }

        SampleProductDetailScreen(this) {
            checkBuyByPayFormButtonIsVisible()
        }
    }

    private fun setStubs(amount: Long = 10000, checkType: CheckType) {
        stubFor(GetTerminalPayMethodsMock.matcher, willReturn = GetTerminalPayMethodsMock.success())
        stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.successNoActiveCard())
        stubFor(InitMock.matcher, willReturn = InitMock.success(amount = amount))

        when (checkType) {
            CheckType.NO -> {
                stubFor(Check3dsVersionMock.matcher, willReturn = Check3dsVersionMock.non3ds())
                stubFor(ChallengeMock.matcher, willReturn = ChallengeMock.success())
                stubFor(
                    FinishAuthorizeMock.matcher,
                    willReturn = FinishAuthorizeMock.non3ds(amount = amount)
                )
            }

            CheckType.THREE_DS, CheckType.THREE_DS_HOLD -> {
                stubFor(Check3dsVersionMock.matcher, willReturn = Check3dsVersionMock.success())
                stubFor(ChallengeMock.matcher, willReturn = ChallengeMock.success())
                stubFor(ChallengeCompleteMock.matcher, willReturn = ChallengeCompleteMock.success())

                stubFor(
                    Submit3DSAuthorizationV2Mock.matcher,
                    willReturn = Submit3DSAuthorizationV2Mock.success()
                )
                stubFor(
                    Complete3DSMethodv2Mock.matcher,
                    willReturn = Complete3DSMethodv2Mock.success()
                )
                stubFor(
                    FinishAuthorizeMock.matcher,
                    willReturn = FinishAuthorizeMock.success(amount = amount)
                )
                stubFor(GetStateMock.matcher, willReturn = GetStateMock.success(amount = amount))
            }

            else -> {}
        }
    }
}