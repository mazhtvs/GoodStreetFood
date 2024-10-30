package dataproviders.card

import androidx.test.platform.app.InstrumentationRegistry
import ru.tinkoff.acquiring.sdk.ui.customview.editcard.CardPaymentSystem
import ru.tinkoff.acquiring.sdk.utils.BankIssuer

data class Card(
    var cardId: String,
    var pan: String,
    var status: String,
    var rebillId: String,
    var bank: BankIssuer,
    var bin: String,
    var cardNumber: String,
    var paymentSystem: CardPaymentSystem,
    var cvc: String,
    var expireMonth: String,
    var expireYear: String,
) {
    fun getBankName(): String {
        return bank.getCaption(InstrumentationRegistry.getInstrumentation().targetContext)!!
    }

    fun getTitleForCardList(): String {
        return getBankName() + " • " + pan
    }
}