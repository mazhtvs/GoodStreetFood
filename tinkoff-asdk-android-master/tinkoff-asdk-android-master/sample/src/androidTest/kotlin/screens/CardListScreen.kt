package screens

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Card
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.qameta.allure.kotlin.Allure
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sdk.viewmodel.CardLogoProvider
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen

class CardListScreen(testContext: TestContext<*>) : BaseScreen(testContext) {

    private val addNewCardButton = KButton { withId(R.id.acq_add_new_card) }
    private val anotherCardButton = KButton { withId(R.id.acq_another_card) }
    private val changeButton = KButton { withText("Изменить") }
    private val doneButton = KButton { withText("Готово") }
    private val backButton = KButton { withDrawable(R.drawable.acq_arrow_back) }

    private val cardList =
        KRecyclerView(
            builder = {
                withId(R.id.acq_card_list_view)
            },
            itemTypeBuilder = {
                itemType(::CardItem)
            }
        )

    fun clickOnAddNewCardButton() {
        step("Нажать на кнопку \"Добавить новую\"") {
            addNewCardButton.click()
        }
    }

    fun checkCardLogo(card: Card) {
        cardList {
            lastChild<CardItem> {
                logo.hasDrawable(CardLogoProvider.getCardLogo(card.cardNumber))
                val bankName = card.getBankName()
                title.hasText(bankName + " • " + card.pan)
            }
        }
    }

    fun checkSelectedCard(card: Card) {
        step("Проверить, что галочка стоит напротив карты - ${card.pan}") {
            cardList {
                childWith<CardItem> {
                    withDescendant { withText(card.getTitleForCardList()) }
                } perform {
                    checkmark.isVisible()
                }
            }
        }
    }

    fun checkCardIsNotSelected(card: Card) {
        step("Проверить, что галочка не отображается напротив карты - ${card.pan}") {
            cardList {
                childWith<CardItem> {
                    withDescendant { withText(card.getTitleForCardList()) }
                } perform {
                    checkmark.isGone()
                }
            }
        }
    }

    fun checkCardIsNotDisplayed(card: Card) {
        step("Проверить, что карта не отображается - ${card.pan}") {
            cardList {
                childWith<CardItem> {
                    withDescendant { withText(card.getTitleForCardList()) }
                }.doesNotExist()
            }
        }
    }

    fun clickOnCard(card: Card) {
        step("Нажать на карту - ${card.pan}") {
            cardList {
                childWith<CardItem> {
                    withDescendant { withText(card.getTitleForCardList()) }
                }.click()
            }
        }
    }

    fun clickOnPayWithAnotherCardButton() {
        step("Нажать \"Оплатить другой\"") {
            anotherCardButton.click()
        }
    }

    fun clickOnChangeButton() {
        step("Нажать на кнопку \"Изменить\"") {
            changeButton.click()
        }
    }

    fun clickOnDoneButton() {
        step("Нажать на кнопку \"Готово\"") {
            doneButton.click()
        }
    }

    fun clickOnDeleteButton(card: Card) {
        step("Удалить карту - ${card.getTitleForCardList()}") {
            cardList {
                childWith<CardItem> {
                    withDescendant { withText(card.getTitleForCardList()) }
                } perform {
                    deleteCardButton.click()
                }
            }
        }
    }

    fun clickOnBackButton() {
        step("Нажать кнопку назад") {
            backButton.click()
        }
    }

    companion object {

        const val reportName: String = "Экран сохраненные карты"
        const val snapshotName: String = "CardListScreen"

        inline operator fun invoke(
            testContext: TestContext<*>,
            crossinline block: CardListScreen.() -> Unit,
        ): Unit =
            Allure.step(reportName) { CardListScreen(testContext).block() }
    }

    override val reportName: String = SampleMainScreen.reportName
    override val snapshotName: String = SampleMainScreen.snapshotName
}