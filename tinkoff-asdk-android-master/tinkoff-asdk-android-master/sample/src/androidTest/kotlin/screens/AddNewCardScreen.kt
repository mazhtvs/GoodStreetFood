package screens

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import dataproviders.card.Card
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton
import io.qameta.allure.kotlin.Allure.step
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen
import utils.elements.AcqTextFieldViewElement

class AddNewCardScreen(testContext: TestContext<*>) : BaseScreen(testContext) {

    val cardInputFields = CardDataElements()
    private val addCardButton = KButton { withId(R.id.acq_attach_btn_attach) }

    /**
     * Проверки
     */
    fun checkAddCardButtonIsEnabled() {
        step("Проверить, что кнопка \"Добавить\" активна") {
            addCardButton.isEnabled()
        }
    }

    fun checkAddCardButtonIsDisabled() {
        step("Проверить, что кнопка \"Добавить\" не активна") {
            addCardButton.isDisabled()
        }
    }

    fun clickOnAddCardButton() {
        step("Нажать на кнопку \"Добавить\"") {
            addCardButton.click()
        }
    }

    companion object {

        const val reportName: String = "Экран добавления новой карты"
        const val snapshotName: String = "AddNewCardPage"

        inline operator fun invoke(
                testContext: TestContext<*>,
                crossinline block: AddNewCardScreen.() -> Unit,
        ): Unit =
                step(reportName) { AddNewCardScreen(testContext).block() }
    }

    override val reportName: String = AddNewCardScreen.reportName
    override val snapshotName: String = AddNewCardScreen.snapshotName
}
