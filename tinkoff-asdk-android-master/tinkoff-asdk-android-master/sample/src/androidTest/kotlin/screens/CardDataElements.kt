package screens

import dataproviders.card.Card
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.text.KButton
import io.qameta.allure.kotlin.Allure.step
import ru.tinkoff.acquiring.sample.R
import utils.elements.AcqTextFieldViewElement

class CardDataElements {

    private val cardNumberField = AcqTextFieldViewElement { withId(R.id.card_number_input) }
    private val expireDateField = AcqTextFieldViewElement { withId(R.id.expiry_date_input) }
    private val cvcField = AcqTextFieldViewElement {
        withId(R.id.cvc_input)
        withParent {
            withParent {
                withId(R.id.fragment_card_data_input)
            }
        }
    }
    private val scanCardButton = KButton { withId(R.id.acq_ic_card_frame) }
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
                withParent {
                    withParent {
                        withId(R.id.fragment_card_data_input)
                    }
                }
            }
        }
    }

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

    fun enterCardData(card: Card) {
        step("Ввести карточные данные") {
            typeCardNumber(card.cardNumber)
            typeExpireDate(card.expireMonth, card.expireYear)
            typeCVC(card.cvc)
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


}