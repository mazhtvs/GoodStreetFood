package dataproviders.card

import ru.tinkoff.acquiring.sdk.ui.customview.editcard.CardPaymentSystem
import ru.tinkoff.acquiring.sdk.utils.BankIssuer
import utils.extentions.randomExcluding

object CardFactory {

    private const val PAN_SIZE = 4
    private val cardIdRange = 10000000..99999999
    private val rebillIdRange = 1000..9999

    fun generateRandomCard(): Card {
        val cardId = cardIdRange.random().toString()
        val status = "A"
        val rebillId = rebillIdRange.random().toString()
        val bank = BankIssuer.values().randomExcluding(BankIssuer.OTHER, BankIssuer.UNKNOWN)
        val bin = bank.bins.random()
        val cardNumber = generateCreditCardNumber(bin)
        val pan = cardNumber.takeLast(PAN_SIZE)
        val paymentSystem = CardPaymentSystem.resolve(cardNumber)
        val cvc = generateRandomCVC()
        val expireMonth = generateMonth()
        val expireYear = generateYear()
        return Card(
            cardId = cardId,
            pan = pan,
            status = status,
            rebillId = rebillId,
            bank = bank,
            bin = bin,
            cardNumber = cardNumber,
            paymentSystem = paymentSystem,
            cvc = cvc,
            expireMonth = expireMonth,
            expireYear = expireYear
        )
    }

    private fun generateCreditCardNumber(bin: String): String {
        /* Extract the first 6 digits of the bank card (BIN) and convert them to integers. */
        val cardNumber = bin.mapNotNull { it.toString().toIntOrNull() }.toMutableList()

        /* Fill the array with random numbers 0-9 */
        repeat(9) {
            cardNumber.add((0..9).random())
        }

        /* Calculate the final digit using a custom variation of Luhn's formula
           This way we don't have to spend time reversing the list
        */
        val lastDigit = calculateFinalDigitViaLuhnsFormula(cardNumber)
        cardNumber.add(lastDigit)

        // Convert card number list to string
        return cardNumber.joinToString("")
    }

    private fun calculateFinalDigitViaLuhnsFormula(cardNumber: MutableList<Int>): Int {
        var sum = 0
        val cardLength = cardNumber.size - 1
        for ((index, value) in cardNumber.withIndex()) {
            val isReversedIndexOdd = (cardLength - index) % 2 == 1
            val addition = when {
                isReversedIndexOdd -> value
                value * 2 > 9 -> value * 2 - 9
                else -> value * 2
            }
            sum += addition
        }
        return 10 - (sum % 10) % 10
    }

    private fun generateRandomCVC(): String {
        val randomCVC = (0..999).random()
        return String.format("%03d", randomCVC)
    }

    private fun generateMonth(): String {
        val randomMonth = (1..12).random()
        return String.format("%02d", randomMonth)
    }

    private fun generateYear(): String {
        val randomYear = (0..99).random()
        return String.format("%02d", randomYear)
    }
}