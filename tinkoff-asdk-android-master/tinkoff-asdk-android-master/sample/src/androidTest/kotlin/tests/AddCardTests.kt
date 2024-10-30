package tests

import dataproviders.card.Card
import dataproviders.card.CardFactory
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Story
import io.qameta.allure.kotlin.junit4.AllureRunner
import mocks.AddCardMock
import mocks.AttachCardMock
import mocks.threeds.ChallengeMock
import mocks.Check3dsVersionMock
import mocks.GetAddCardStateMock
import mocks.GetCardListMock
import mocks.Submit3DSAuthorizationV2Mock
import mocks.threeds.ChallengeCompleteMock
import mocks.threeds.Complete3DSMethodv2Mock
import mocks.threeds.TdsmCompleteMock
import mocks.threeds.TdsmMock
import org.junit.Test
import org.junit.runner.RunWith
import ru.tinkoff.acquiring.sample.ui.MainActivity
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.core.testing.mock.stubs_factory.AllureStepMock.stubFor
import screens.AddNewCardScreen
import screens.AlertElement
import screens.CardListScreen
import screens.SampleMainScreen
import screens.SampleNotificationScreen
import screens.ThreeDSWebViewScreen
import utils.SdkSettings

@RunWith(AllureRunner::class)
@Epic("Automated")
@Story("Управление картами")
class AddCardTests : BaseTest<MainActivity>(MainActivity::class.java) {

    @Test
    fun checkTypeNO() = checkAddCard(CheckType.NO)

    @Test
    fun checkTypeHOLD() = checkAddCard(CheckType.HOLD)

    @Test
    fun checkType3DS() = checkAddCard(CheckType.THREE_DS)

    @Test
    fun checkType3DSHOLD() = checkAddCard(CheckType.THREE_DS_HOLD)

    @Test
    fun checkType3DSWithError() {
        run {
            SdkSettings.setCheckType(CheckType.THREE_DS)
            val card = CardFactory.generateRandomCard()
            setStubs(card, CheckType.THREE_DS)

            stubFor(AttachCardMock.matcher, willReturn = AttachCardMock.threeDs(card))
            stubFor(Check3dsVersionMock.matcher, willReturn = Check3dsVersionMock.non3ds())

            SampleMainScreen(this) {
                clickOnMoreButton()
                clickOnSavedCard()
            }

            CardListScreen(this) {
                clickOnAddNewCardButton()
            }

            AddNewCardScreen(this) {
                cardInputFields.enterCardData(card)
                clickOnAddCardButton()
            }

            AlertElement(this).checkAlert(
                titleText = "У нас проблема, мы уже решаем ее",
                messageText = "Попробуйте снова через пару минут",
                buttonText = "ПОНЯТНО"
            )
        }
    }

    @Test
    fun addCardFromMerchantApp() {
        run {
            SdkSettings.setCheckType(CheckType.THREE_DS)
            val card = CardFactory.generateRandomCard()
            setStubs(card, CheckType.THREE_DS)
            stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.addCardToList(card))

            SampleMainScreen(this) {
                clickOnAttachCard()
            }

            AddNewCardScreen(this) {
                cardInputFields.checkCardNumberFieldIsFocused()
                cardInputFields.checkCardNumberField()
                cardInputFields.checkExpireField()
                cardInputFields.checkCVCField()

                checkAddCardButtonIsDisabled()
                cardInputFields.enterCardData(card)

                device.uiDevice.pressBack()

                checkAddCardButtonIsEnabled()
                clickOnAddCardButton()
            }

            ThreeDSWebViewScreen(this) {
                sendOtp("1111")
            }

            SampleNotificationScreen(this) {
                checkText("Привязка карты с ID=${card.cardId}\nпрошла успешно")
            }
        }
    }

    private fun checkAddCard(checkType: CheckType) {
        run {
            SdkSettings.setCheckType(checkType)
            val card = CardFactory.generateRandomCard()
            setStubs(card, checkType)

            SampleMainScreen(this) {
                clickOnMoreButton()
                clickOnSavedCard()
            }

            CardListScreen(this) {
                clickOnAddNewCardButton()
            }

            stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.addCardToList(card))

            AddNewCardScreen(this) {
                cardInputFields.checkCardNumberFieldIsFocused()
                cardInputFields.checkCardNumberField()
                cardInputFields.checkExpireField()
                cardInputFields.checkCVCField()

                checkAddCardButtonIsDisabled()
                cardInputFields.enterCardData(card)

                device.uiDevice.pressBack()

                checkAddCardButtonIsEnabled()
                clickOnAddCardButton()
            }

            when (checkType) {
                CheckType.THREE_DS, CheckType.THREE_DS_HOLD -> {
                    ThreeDSWebViewScreen(this) {
                        checkIsDisplayed()
                        sendOtp("1111")
                    }
                }
                else -> {}
            }

            CardListScreen(this) {
                checkCardLogo(card)
            }
        }
    }

    private fun setStubs(card: Card, checkType: CheckType) {
        stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.success())

        when (checkType) {
            CheckType.HOLD, CheckType.NO -> {
                stubFor(AddCardMock.matcher, willReturn = AddCardMock.success())
                stubFor(AttachCardMock.matcher, willReturn = AttachCardMock.success(card))
                stubFor(GetAddCardStateMock.matcher, willReturn = GetAddCardStateMock.success())
            }

            CheckType.THREE_DS, CheckType.THREE_DS_HOLD -> {
                stubFor(AddCardMock.matcher, willReturn = AddCardMock.threeDs())
                stubFor(AttachCardMock.matcher, willReturn = AttachCardMock.threeDs(card))
                stubFor(GetAddCardStateMock.matcher, willReturn = GetAddCardStateMock.threeDs(card))
                stubFor(Check3dsVersionMock.matcher, willReturn = Check3dsVersionMock.success())
                stubFor(ChallengeMock.matcher, willReturn = ChallengeMock.success())
                stubFor(ChallengeCompleteMock.matcher, willReturn = ChallengeCompleteMock.success())
                stubFor(TdsmMock.matcher, willReturn = TdsmMock.success())
                stubFor(TdsmCompleteMock.matcher, willReturn = TdsmCompleteMock.success())
                stubFor(
                    Submit3DSAuthorizationV2Mock.matcher,
                    willReturn = Submit3DSAuthorizationV2Mock.success()
                )
                stubFor(
                    Complete3DSMethodv2Mock.matcher,
                    willReturn = Complete3DSMethodv2Mock.success()
                )
            }
        }
    }
}