package screens

import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton
import io.qameta.allure.kotlin.Allure.step
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.core.testing.kaspresso.screens.BaseScreen
import utils.elements.AcqTextFieldViewElement

class AddNewCardPage(testContext: TestContext<*>) : BaseScreen(testContext) {

    private val cardNumberField = AcqTextFieldViewElement { withId(R.id.card_number_input) }
    private val expireDateField = AcqTextFieldViewElement { withId(R.id.expiry_date_input) }
    private val cvcField = AcqTextFieldViewElement { withId(R.id.cvc_input) }
    private val scanCardButton = KButton { withId(R.id.acq_ic_card_frame) }
    private val addCardButton = KButton { withId(R.id.acq_attach_btn_attach) }

    private val cardNumberEditText = KEditText {
        withId(R.id.edit_text)
        withParent {
            withParent {
                withId(R.id.card_number_input)
            }
        }
    }

    private val expireDateEditText = KEditText {
        withId(R.id.edit_text)
        withParent {
            withParent {
                withId(R.id.expiry_date_input)
            }
        }
    }

    private val cvcEditText = KEditText {
        withId(R.id.edit_text)
        withParent {
            withParent {
                withId(R.id.cvc_input)
            }
        }
    }

    /**
     * Проверки
     */
    fun checkCardNumberField() {
        step("Проверить отображение поля \"Номер\"") {
            cardNumberField.isDisplayed()
            cardNumberEditText.isDisplayed()
            cardNumberField.hasTitle("Номер")
            scanCardButton.isDisplayed()
        }
    }

    fun checkExpireField() {
        step("Проверить отображение поля \"Срок\"") {
            expireDateField.isDisplayed()
            expireDateEditText.isDisplayed()
            expireDateField.hasTitle("Срок")
        }
    }

    fun checkCVCField() {
        step("Проверить отображение поля \"CVC\"") {
            cvcField.isDisplayed()
            cvcEditText.isDisplayed()
            cvcField.hasTitle("CVC")
        }
    }

    fun checkCardNumberFieldIsFocused() {
        step("Проверка, что фокус в поле ввода \"Номер\"") {
            cardNumberField.isFocused()
        }
    }

    fun typeCardNumber(number: String) {
        step("Ввести номер карты - $number") {
            cardNumberEditText.typeText(number)
        }
    }

    fun typeExpireDate(month: String, year: String) {
        step("Ввести срок - $month/$year") {
            expireDateEditText {
                typeText("$month$year")
            }
        }
    }

    fun typeCVC(cvc: String) {
        step("Ввести CVC - $cvc") {
            cvcEditText {
                typeText(cvc)
            }
        }
    }

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
            crossinline block: AddNewCardPage.() -> Unit,
        ): Unit =
            step(reportName) { AddNewCardPage(testContext).block() }
    }

    override val reportName: String = AddNewCardPage.reportName
    override val snapshotName: String = AddNewCardPage.snapshotName
}
