package ru.tinkoff.acquiring.sdk.redesign.cards.list.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.tinkoff.acquiring.sdk.models.options.screen.AttachCardOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ChooseCardLauncher
import ru.tinkoff.acquiring.sdk.utils.getExtra

/**
 * @author s.y.biryukov
 */
internal class CardListActivityViewModel(
    stateHandle: SavedStateHandle
) : ViewModel() {

    private val savedCardsOptions = stateHandle.getExtra<SavedCardsOptions>()
    private val paymentOptions =
        stateHandle.get<PaymentOptions>(ChooseCardLauncher.Contract.EXTRA_PAYMENT_OPTIONS)

    private val _stateFlow = MutableStateFlow<CardListFlowState>(CardListFlowState.OpenCardsList(savedCardsOptions))
    val stateFlow = _stateFlow.asStateFlow()
    private val _commandFlow = MutableSharedFlow<CardListFlowCommand>()
    val commandFlow = _commandFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            _stateFlow.emit(CardListFlowState.OpenCardsList(savedCardsOptions))
        }
    }

    fun onPayByNewCard() {
        val options = requireNotNull(paymentOptions) { "Не задан параметр PaymentOptions" }

        viewModelScope.launch {
            _commandFlow.emit(
                CardListFlowCommand.OpenPayByNewCard(
                    paymentOptions = options
                )
            )
        }
    }

    fun onAttachCard() {
        val attachCardOptions = AttachCardOptions().setOptions {
            setTerminalParams(savedCardsOptions.terminalKey, savedCardsOptions.publicKey)
            customerOptions {
                checkType = savedCardsOptions.customer.checkType
                customerKey = savedCardsOptions.customer.customerKey
            }
            features = savedCardsOptions.features
        }

        viewModelScope.launch {
            _commandFlow.emit(
                CardListFlowCommand.OpenAttachCard(
                    attachCardOptions = attachCardOptions
                )
            )
        }
    }

    fun onAttachCardSuccess(
        cardId: String,
        panSuffix: String
    ) {
        viewModelScope.launch {
            _commandFlow.emit(
                CardListFlowCommand.ShowCardAddSuccess(
                    cardId = cardId,
                    panSuffix = panSuffix
                )
            )
            _commandFlow.emit(CardListFlowCommand.RefreshCardList)
        }
    }

    internal sealed interface CardListFlowState {
        data class OpenCardsList(
            val savedCardsOptions: SavedCardsOptions
        ) : CardListFlowState
    }
    internal sealed interface CardListFlowCommand {
        data class OpenPayByNewCard(
            val paymentOptions: PaymentOptions
        ) : CardListFlowCommand

        data class OpenAttachCard(
            val attachCardOptions: AttachCardOptions
        ) : CardListFlowCommand

        data class ShowCardAddSuccess(
            val cardId: String,
            val panSuffix: String,
        ) : CardListFlowCommand

        data object RefreshCardList : CardListFlowCommand
    }
}
