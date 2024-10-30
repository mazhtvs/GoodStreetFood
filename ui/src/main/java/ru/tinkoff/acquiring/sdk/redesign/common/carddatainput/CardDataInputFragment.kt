package ru.tinkoff.acquiring.sdk.redesign.common.carddatainput

import android.content.Context
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.cardscanners.delegate.CardScannerContract
import ru.tinkoff.acquiring.sdk.cardscanners.delegate.CardScannerWrapper
import ru.tinkoff.acquiring.sdk.cardscanners.delegate.ScannedCardResult
import ru.tinkoff.acquiring.sdk.smartfield.AcqTextFieldView
import ru.tinkoff.acquiring.sdk.smartfield.BaubleCardLogo
import ru.tinkoff.acquiring.sdk.smartfield.BaubleClearButton
import ru.tinkoff.acquiring.sdk.smartfield.BaubleClearOrScanButton
import ru.tinkoff.acquiring.sdk.ui.customview.editcard.CardPaymentSystem
import ru.tinkoff.acquiring.sdk.ui.customview.editcard.CardPaymentSystem.MASTER_CARD
import ru.tinkoff.acquiring.sdk.ui.customview.editcard.CardPaymentSystem.VISA
import ru.tinkoff.acquiring.sdk.ui.customview.editcard.validators.CardValidator
import ru.tinkoff.acquiring.sdk.utils.SimpleTextWatcher.Companion.afterTextChanged
import ru.tinkoff.acquiring.sdk.utils.lazyView
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

internal class CardDataInputFragment : Fragment() {

    var onComplete: ((CardDataInputFragment) -> Unit)? = null
    var onNext: ((View) -> Boolean)? = null

    val cardNumberInput: AcqTextFieldView by lazyView(R.id.card_number_input)
    val expiryDateInput: AcqTextFieldView by lazyView(R.id.expiry_date_input)
    val cvcInput: AcqTextFieldView by lazyView(R.id.cvc_input)

    val cardNumber get() = CardNumberFormatter.normalize(cardNumberInput.text)
    val expiryDate get() = expiryDateInput.text.orEmpty()
    val cvc get() = cvcInput.text.orEmpty()

    var useSecureKeyboard: Boolean = false

    private val scannedCardCallback = { it: ScannedCardResult ->
        when (it) {
            is ScannedCardResult.Success -> handleCardScannerSuccessResult(it)
            is ScannedCardResult.Cancel -> Unit
            is ScannedCardResult.Failure -> Unit
        }
    }
    private lateinit var cardScannerWrapper: CardScannerWrapper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cardScannerWrapper = CardScannerWrapper(requireActivity(), scannedCardCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.acq_fragment_card_data_input, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(cardNumberInput) {
            useSecureKeyboard = this@CardDataInputFragment.useSecureKeyboard
            BaubleCardLogo().attach(this)
            BaubleClearOrScanButton().attach(this, cardScannerWrapper)
            val cardNumberFormatter = CardNumberFormatter().also {
                editText.addTextChangedListener(it)
            }

            editText.afterTextChanged {
                editText.errorHighlighted = false

                val cardNumber = cardNumber
                val paymentSystem = CardPaymentSystem.resolve(cardNumber)

                if (cardNumber.startsWith("0")) {
                    editText.errorHighlighted = true
                    return@afterTextChanged
                }

                if (cardNumber.length in paymentSystem.range) {
                    if (!CardValidator.validateCardNumber(cardNumber)) {
                        editText.errorHighlighted = true
                    } else if (shouldAutoSwitchFromCardNumber(cardNumber, paymentSystem)) {
                        expiryDateInput.requestViewFocus()
                    }
                }

                onDataChanged()
            }

            nextPressedListener = onNext?.let { { it(cardNumberInput) } }
        }

        with(expiryDateInput) {
            useSecureKeyboard = this@CardDataInputFragment.useSecureKeyboard
            BaubleClearButton().attach(this)
            MaskFormatWatcher(createExpiryDateMask()).installOn(editText)

            editText.afterTextChanged {
                editText.errorHighlighted = false

                val expiryDate = expiryDate
                if (expiryDate.length >= EXPIRY_DATE_MASK.length) {
                    if (CardValidator.validateExpireDate(expiryDate, false)) {
                        cvcInput.requestViewFocus()
                    } else {
                        editText.errorHighlighted = true
                    }
                }

                onDataChanged()
            }

            nextPressedListener = onNext?.let { { it(expiryDateInput) } }
        }

        with(cvcInput) {
            useSecureKeyboard = this@CardDataInputFragment.useSecureKeyboard
            editText.letterSpacing = 0.1f
            BaubleClearButton().attach(this)
            transformationMethod = PasswordTransformationMethod()
            MaskFormatWatcher(createCvcMask()).installOn(editText)

            editText.afterTextChanged {
                editText.errorHighlighted = false

                val cvc = cvc
                if (cvc.length >= EXPIRY_DATE_MASK.length) {
                    if (CardValidator.validateSecurityCode(cvc)) {
                        cvcInput.clearViewFocus()
                        if (validate()) {
                            onComplete?.invoke(this@CardDataInputFragment)
                        }
                    } else {
                        editText.errorHighlighted = true
                    }
                }

                onDataChanged()
            }

            nextPressedListener = onNext?.let { { it(cvcInput) } }
        }

        cardNumberInput.requestViewFocus()

        savedInstanceState?.run {
            cardNumberInput.text = getString(SAVE_CARD_NUMBER, cardNumber)
            expiryDateInput.text = getString(SAVE_EXPIRY_DATE, expiryDate)
            cvcInput.text = getString(SAVE_CVC, cvc)
        }

        onDataChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        with(outState) {
            putString(SAVE_CARD_NUMBER, cardNumber)
            putString(SAVE_EXPIRY_DATE, expiryDate)
            putString(SAVE_CVC, cvc)
        }
    }

    fun setupCameraCardScanner(contract: CardScannerContract?) {
        cardScannerWrapper.cameraCardScannerContract = contract
    }

    fun validate(): Boolean {
        var result = true
        if (CardValidator.validateCardNumber(cardNumber)) {
            cardNumberInput.errorHighlighted = true
            result = false
        }
        if (CardValidator.validateExpireDate(expiryDate, false)) {
            expiryDateInput.errorHighlighted = true
            result = false
        }
        if (CardValidator.validateSecurityCode(cvc)) {
            cvcInput.errorHighlighted = true
            result = false
        }
        return result
    }

    fun isValid(): Boolean = CardValidator.validateCardNumber(cardNumber) &&
            CardValidator.validateExpireDate(expiryDate, false) &&
            CardValidator.validateSecurityCode(cvc)

    private fun onDataChanged() {
        ((parentFragment as? OnCardDataChanged) ?: (activity as? OnCardDataChanged))
            ?.onCardDataChanged(isValid())
    }

    fun clearInput() {
        cardNumberInput.text = ""
        expiryDateInput.text = ""
        cvcInput.text = ""
    }

    fun clearFocus() {
        cardNumberInput.clearFocus()
        expiryDateInput.clearFocus()
        cvcInput.clearFocus()
    }

    private fun handleCardScannerSuccessResult(result: ScannedCardResult.Success) {
        val scannedCardNumber = result.data.cardNumber
        cardNumberInput.text = scannedCardNumber

        val scannedDate = result.data.expireDate
        expiryDateInput.text = scannedDate

        when {
            scannedCardNumber.isBlank() -> {
                cardNumberInput.requestViewFocus()
            }
            scannedDate.isBlank() -> {
                expiryDateInput.requestViewFocus()
            }
            else -> {
                cvcInput.requestViewFocus()
            }
        }
    }

    fun interface OnCardDataChanged {
        fun onCardDataChanged(isValid: Boolean)
    }

    private companion object {

        const val MIN_LENGTH_FOR_AUTO_SWITCH = 16

        const val EXPIRY_DATE_MASK = "__/__"
        const val CVC_MASK = "___"

        private const val SAVE_CARD_NUMBER = "extra_card_number"
        private const val SAVE_EXPIRY_DATE = "extra_expiry_date"
        private const val SAVE_CVC = "extra_save_cvc"

        fun shouldAutoSwitchFromCardNumber(cardNumber: String, paymentSystem: CardPaymentSystem): Boolean {
            if (cardNumber.length == paymentSystem.range.last) return true

            if ((paymentSystem == VISA || paymentSystem == MASTER_CARD) &&
                cardNumber.length >= MIN_LENGTH_FOR_AUTO_SWITCH) {
                return true
            }
            return false
        }

        fun createExpiryDateMask(): MaskImpl = MaskImpl
            .createTerminated(UnderscoreDigitSlotsParser().parseSlots(EXPIRY_DATE_MASK))

        fun createCvcMask(): MaskImpl = MaskImpl
            .createTerminated(UnderscoreDigitSlotsParser().parseSlots(CVC_MASK))
    }
}
