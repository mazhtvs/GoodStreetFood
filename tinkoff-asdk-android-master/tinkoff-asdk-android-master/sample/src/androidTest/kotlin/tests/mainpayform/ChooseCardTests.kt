package tests.mainpayform

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Card
import dataproviders.card.CardFactory
import io.qameta.allure.kotlin.AllureId
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Story
import io.qameta.allure.kotlin.junit4.AllureRunner
import mocks.Check3dsVersionMock
import mocks.FinishAuthorizeMock
import mocks.GetCardListMock
import mocks.InitMock
import mocks.RemoveCardMock
import mocks.models.PayMethod
import mocks.threeds.ChallengeMock
import org.junit.Test
import org.junit.runner.RunWith
import ru.tinkoff.core.testing.mock.stubs_factory.AllureStepMock.stubFor
import screens.CardListScreen
import screens.PaymentStatusSheet
import screens.MainPayFormScreen
import screens.PayNewCardScreen
import screens.SampleProductDetailScreen

@RunWith(AllureRunner::class)
@Epic("Automated")
@Story("Оплата картой")
class ChooseCardTests : MainPayFormTests() {

    private val firstCard: Card = CardFactory.generateRandomCard()
    private val secondCard: Card = CardFactory.generateRandomCard()

    @Test
    @AllureId("2523527")
    fun selectAnotherFromList() {
        run {
            prepareApp(addScheme = true)

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(firstCard, "Оплатить 20,99 ₽")
                clickOnChangeCardButton()
            }

            CardListScreen(this) {
                checkSelectedCard(firstCard)
                checkCardIsNotSelected(secondCard)
                clickOnCard(secondCard)
            }

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(secondCard, "Оплатить 20,99 ₽")
                clickOnChangeCardButton()
            }

            CardListScreen(this) {
                checkSelectedCard(secondCard)
            }
        }
    }

    @Test
    @AllureId("2523528")
    fun payWithNewCard() {
        run {
            prepareApp(addScheme = true)
            setStubsForPayment()

            MainPayFormScreen(this) {
                clickOnChangeCardButton()
            }

            CardListScreen(this) {
                clickOnPayWithAnotherCardButton()
            }

            PayNewCardScreen(this) {
                checkScreenTitle()
                checkPayByNewCardButtonIsDisable()
                cardInputFields.enterCardData(firstCard)
                checkPayByNewCardButtonIsEnabled()
                clickPayByNewCardButton()
            }

            PaymentStatusSheet(this) {
                checkSheetContents(titleText = "Оплачено", mainButtonText = "Понятно")
                mainButtonClick()
            }

            SampleProductDetailScreen(this) {
                checkBuyByPayFormButtonIsVisible()
            }
        }
    }

    @Test
    @AllureId("2523529")
    fun changeCardByDeletingCurrent() {
        run {
            prepareApp(addScheme = true)
            stubFor(RemoveCardMock.matcher, willReturn = RemoveCardMock.success())

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(firstCard, "Оплатить 20,99 ₽")
                clickOnChangeCardButton()
            }

            CardListScreen(this) {
                checkSelectedCard(firstCard)
                clickOnChangeButton()
                clickOnDeleteButton(firstCard)
                checkCardIsNotDisplayed(firstCard)
                clickOnDoneButton()
                checkSelectedCard(secondCard)
                clickOnBackButton()
            }

            MainPayFormScreen(this) {
                checkPrimaryForSavedCard(secondCard, "Оплатить 20,99 ₽")
            }
        }
    }

    private fun TestContext<Unit>.prepareApp(addScheme: Boolean, vararg methods: PayMethod) {
        setGetTerminalPayMethodsMock(addScheme, *methods)
        stubFor(GetCardListMock.matcher, willReturn = GetCardListMock.withCards(firstCard, secondCard))
        openPayForm()
    }

    private fun setStubsForPayment() {
        stubFor(InitMock.matcher, willReturn = InitMock.success(amount = 10000))
        stubFor(Check3dsVersionMock.matcher, willReturn = Check3dsVersionMock.non3ds())
        stubFor(ChallengeMock.matcher, willReturn = ChallengeMock.success())
        stubFor(
            FinishAuthorizeMock.matcher,
            willReturn = FinishAuthorizeMock.non3ds(amount = 10000)
        )
    }
}