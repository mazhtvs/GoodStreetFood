package ru.tinkoff.acquiring.sdk.redesign.payment.ui

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.enums.CardStatus
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.paysources.AttachedCard
import ru.tinkoff.acquiring.sdk.models.paysources.CardData
import ru.tinkoff.acquiring.sdk.models.paysources.CardSource
import ru.tinkoff.acquiring.sdk.payment.PaymentByCardProcess
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.models.EmailValidator
import ru.tinkoff.acquiring.sdk.redesign.payment.PaymentByCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.payment.model.CardChosenModel
import ru.tinkoff.acquiring.sdk.redesign.payment.ui.PaymentByCardFragment.Companion.ARG_PAYMENT_CARDS_OPTION
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper
import ru.tinkoff.acquiring.sdk.utils.BankCaptionProvider
import ru.tinkoff.acquiring.sdk.utils.BankCaptionResourceProvider
import ru.tinkoff.acquiring.sdk.utils.lazyUnsafe

// todo - раздельная vm  для сохраненной карты и новой
internal class PaymentByCardViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val paymentByCardProcess: PaymentByCardProcess,
    private val bankCaptionProvider: BankCaptionProvider,
) : ViewModel() {

    private val startData: PaymentByCardLauncher.StartData by lazyUnsafe {
        savedStateHandle.get<PaymentByCardLauncher.StartData>(ARG_PAYMENT_CARDS_OPTION)!!
    }
    private val chosenCard by lazyUnsafe {
        startData.cards.firstOrNull { it.status == CardStatus.ACTIVE }?.let {
            CardChosenModel(it, bankCaptionProvider(it.pan!!))
        }
    }

    private val _isCardSaved = MutableStateFlow<Boolean?>(chosenCard != null)
    var isCardSaved: StateFlow<Boolean?> = _isCardSaved

    fun isCardSavedFocusChanger(): Boolean {
        return isCardSaved.value == true
    }

    val paymentProcessState = paymentByCardProcess.state

    val state: MutableStateFlow<State> =
        MutableStateFlow(
            State(
                cardId = chosenCard?.id,
                isValidEmail = startData.paymentOptions.customer.email.isNullOrBlank(),
                sendReceipt = startData.paymentOptions.customer.email.isNullOrBlank().not(),
                email = startData.paymentOptions.customer.email,
                paymentOptions = startData.paymentOptions,
                chosenCard = chosenCard
            )
        )

    // ручной ввод карты
    fun setCardData(
        cardNumber: String? = null,
        cvc: String? = null,
        dateExpired: String? = null,
        isValidCardData: Boolean = false,
    ) {
        if (state.value.chosenCard != null) return

        state.update {
            it.copy(
                cardNumber = cardNumber,
                cvc = cvc,
                dateExpired = dateExpired,
                isValidCardData = isValidCardData,
                cardId = null,
            )
        }
    }

    // ввод сохраненной карты
    fun setSavedCard(card: Card) = state.update {
        val isCardChanged = it.cardId != card.cardId
        it.copy(
            cardId = card.cardId,
            cardNumber = card.pan,
            cvc = if (isCardChanged) null else it.cvc, // Очищаем CVC только если карта изменена
            dateExpired = card.expDate,
            isValidCardData = it.cvc?.length == 3,
            chosenCard = CardChosenModel(card, bankCaptionProvider(card.pan!!))
        )
    }


    // ввод кода сохраненной карты
    fun setCvc(cvc: String, isValid: Boolean) =
        state.update { it.copy(cvc = cvc, isValidCardData = isValid) }

    fun setInputNewCard() = state.update {
        it.copy(
            cardId = null,
            cardNumber = null,
            cvc = null,
            dateExpired = null,
            isValidCardData = false,
            isValidEmail = it.isValidEmail,
            chosenCard = null
        )
    }

    fun sendReceiptChange(isSelect: Boolean) = state.update {
        it.copy(sendReceipt = isSelect, isValidEmail = EmailValidator.validate(it.email))
    }

    fun setEmail(email: String?, isValidEmail: Boolean) = state.update {
        it.copy(email = email, isValidEmail = isValidEmail)
    }

    fun pay() {
        val _state = state.value
        val emailForPayment = if (_state.sendReceipt) _state.email else null

        paymentByCardProcess.start(
            cardData = _state.cardSource,
            paymentOptions = _state.paymentOptions,
            email = emailForPayment
        )
    }

    fun rechoseCard() {
        paymentByCardProcess.recreate()
    }

    fun goTo3ds() {
        paymentByCardProcess.goTo3ds()
    }

    fun cancelPayment() {
        paymentByCardProcess.stop()
    }

    data class State(
        val cardId: String? = null,
        private val cardNumber: String? = null,
        val cvc: String? = null,
        private val dateExpired: String? = null,
        private val isValidCardData: Boolean = false,
        val isValidEmail: Boolean = false,
        val chosenCard: CardChosenModel? = null,
        val sendReceipt: Boolean = false,
        val email: String? = null,
        val paymentOptions: PaymentOptions,
    ) {

        val buttonEnabled: Boolean = if (sendReceipt) {
            isValidCardData && isValidEmail
        } else {
            isValidCardData
        }

        val amount = paymentOptions.order.amount.toHumanReadableString()

        val cardSource: CardSource
            get() {
                return if (cardId != null)
                    AttachedCard(cardId, cvc)
                else
                    CardData(cardNumber!!, dateExpired!!, cvc!!)
            }
    }

    companion object {
        fun factory(application: Application, paymentOptions: PaymentOptions) = viewModelFactory {
            val acq = TinkoffAcquiring(
                context = application,
                terminalKey = paymentOptions.terminalKey,
                publicKey = paymentOptions.publicKey
            )
            PaymentByCardProcess.init(
                sdk = acq.sdk,
                application = application,
                threeDsDataCollector = ThreeDsHelper.CollectData
            )

            initializer {
                PaymentByCardViewModel(
                    createSavedStateHandle(),
                    PaymentByCardProcess.get(),
                    BankCaptionResourceProvider(application)
                )
            }
        }
    }
}
