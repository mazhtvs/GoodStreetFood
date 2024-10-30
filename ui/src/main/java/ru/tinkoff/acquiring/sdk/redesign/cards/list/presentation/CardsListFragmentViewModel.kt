package ru.tinkoff.acquiring.sdk.redesign.cards.list.presentation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.exceptions.checkCustomerNotFoundError
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.enums.CardStatus
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.redesign.cards.list.models.CardItemUiModel
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardListEvent
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardListMode
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardListNav
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardsListFragment
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardsListState
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.MenuMode
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.ScreenMode
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.ScreenState
import ru.tinkoff.acquiring.sdk.responses.GetCardListResponse
import ru.tinkoff.acquiring.sdk.utils.BankCaptionProvider
import ru.tinkoff.acquiring.sdk.utils.ConnectionChecker
import ru.tinkoff.acquiring.sdk.utils.CoroutineManager
import ru.tinkoff.acquiring.sdk.utils.checkNotNull

/**
 * Created by Ivan Golovachev
 */
internal class CardsListFragmentViewModel(
    savedStateHandle: SavedStateHandle,
    private val sdk: AcquiringSdk,
    private val connectionChecker: ConnectionChecker,
    private val bankCaptionProvider: BankCaptionProvider,
    private val manager: CoroutineManager = CoroutineManager()
) : ViewModel() {

    private val savedCardsOptions = savedStateHandle.get<SavedCardsOptions>(CardsListFragment.ARG_SAVED_CARDS_OPTION)
        .checkNotNull { "Не передан аргумент ${CardsListFragment.ARG_SAVED_CARDS_OPTION}" }

    private val selectedCardIdFlow = MutableStateFlow(savedCardsOptions.features.selectedCardId)

    private var deleteJob: Job? = null

    @VisibleForTesting
    val stateFlow = MutableStateFlow(ScreenState.DEFAULT.copy(
        allowNewCard = savedCardsOptions.allowNewCard,
        withArrowBack = savedCardsOptions.withArrowBack,
        screenMode = when(savedCardsOptions.mode) {
            SavedCardsOptions.Mode.LIST -> ScreenMode.LIST
            SavedCardsOptions.Mode.PAYMENT -> ScreenMode.PAYMENT
        }
    ))

    val stateUiFlow = stateFlow.asStateFlow()

    val eventFlow = MutableStateFlow<CardListEvent?>(null)

    @VisibleForTesting
    val navigationChannel = Channel<CardListNav>()

    val navigationFlow = navigationChannel.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val customerKey = savedCardsOptions.customer.customerKey
        if (connectionChecker.isOnline().not()) {
            stateFlow.update {
                it.copy(
                    listState = CardsListState.NoNetwork,
                    menuMode = MenuMode.EMPTY
                )
            }
            return
        }
        stateFlow.update {
            it.copy(
                listState = CardsListState.Shimmer,
                menuMode = MenuMode.EMPTY
            )
        }
        manager.launchOnBackground {
            if (customerKey == null) {
                stateFlow.update {
                    it.copy(
                        listState = CardsListState.Error(Throwable()),
                        menuMode = MenuMode.EMPTY
                    )
                }
                return@launchOnBackground
            }

            sdk.getCardList { this.customerKey = customerKey }.executeFlow().collect { r ->
                r.process(
                    onSuccess = { handleGetCardListResponse(it) },
                    onFailure = ::handleGetCardListError
                )
            }
        }
    }

    fun deleteCard(model: CardItemUiModel) {
        if (deleteJob?.isActive == true) {
            return
        }

        eventFlow.value = CardListEvent.RemoveCardProgress(model)
        deleteJob = manager.launchOnBackground {
            if (connectionChecker.isOnline().not()) {
                eventFlow.value = CardListEvent.ShowError
                return@launchOnBackground
            }
            val customerKey = savedCardsOptions.customer.customerKey
            if (customerKey == null) {
                eventFlow.value = CardListEvent.ShowError
                return@launchOnBackground
            }
            sdk.removeCard {
                this.cardId = model.id
                this.customerKey = customerKey
            }
                .executeFlow()
                .collect {
                    it.process(
                        onSuccess = {
                            handleDeleteCardSuccess(model)
                            deleteJob?.cancel()
                        },
                        onFailure = { e ->
                            eventFlow.value = CardListEvent.ShowCardDeleteError(e)
                            deleteJob?.cancel()
                        }
                    )
                }
        }
    }

    fun returnToBaseMode() {
        val baseMode = resolveBaseCardListMode()
        updateContentState(baseMode)
    }

    fun switchToDeleteMode() {
        updateContentState(CardListMode.DELETE)
    }

    fun chooseCard(model: CardItemUiModel) {
        if (stateFlow.value.screenMode == ScreenMode.PAYMENT) {
            navigationChannel.trySend(CardListNav.FinishWithSelectCard(model.card))
        }
    }

    fun onAttachCardSuccess(cardPan: String) {
        eventFlow.value = CardListEvent.ShowCardAttachDialog(cardPan)
        loadData()
    }

    fun onPayByAnotherCardClicked() {
        payByNewCard()
    }

    fun onAddNewCardClicked() = viewModelScope.launch {
        goToAttachCard()
    }

    fun onErrorButtonClick() {
        val error = stateUiFlow.value.listState as CardsListState.Error
        navigationChannel.trySend(CardListNav.FinishWithError(error.throwable))
    }

    private fun resolveBaseCardListMode(): CardListMode {
        return if (selectedCardIdFlow.value == null) CardListMode.LIST else CardListMode.CHOOSE
    }

    private fun updateContentState(mode: CardListMode) {
        stateFlow.update { state ->
            val prevListState = state.listState
            if (prevListState is CardsListState.Content) {
                val cards = prevListState.cards.map {
                    it.copy(
                        showDelete = mode == CardListMode.DELETE,
                        isBlocked = it.isBlocked,
                        showChoose = selectedCardIdFlow.value == it.card.cardId && mode === CardListMode.CHOOSE
                    )
                }
                state.copy(
                    listMode = mode,
                    allowNewCard = resolveAllowNewCard(mode),
                    menuMode = resolveMenuMode(mode, cards),
                    listState = CardsListState.Content(cards)
                )
            } else {
                state.copy(
                    listMode = mode,
                    allowNewCard = resolveAllowNewCard(mode),
                    menuMode = resolveMenuMode(mode)
                )
            }
        }
    }

    private suspend fun goToAttachCard() {
        val attachCardEvent = CardListNav.ToAttachCard()
        navigationChannel.send(attachCardEvent)
    }

    fun onBackPressed() {
        if (eventFlow.value is CardListEvent.RemoveCardProgress) return

        when (stateFlow.value.listState) {
            is CardsListState.Content -> when (stateFlow.value.screenMode) {
                ScreenMode.LIST -> {
                    navigationChannel.trySend(CardListNav.FinishWithCancel)
                }
                ScreenMode.PAYMENT -> {
                    val selectedCard = (stateFlow.value.listState as? CardsListState.Content)?.cards
                            ?.find { it.id == selectedCardIdFlow.value }
                    if (selectedCard != null) {
                        navigationChannel.trySend(
                            CardListNav.FinishWithSelectCard(selectedCard.card)
                        )
                    } else {
                        navigationChannel.trySend(CardListNav.FinishWithCancel)
                    }
                }
            }

            else -> {
                navigationChannel.trySend(CardListNav.FinishWithCancel)
            }
        }
    }

    private fun handleGetCardListResponse(it: GetCardListResponse) {
        try {
            val mode = if (selectedCardIdFlow.value != null) {
                CardListMode.CHOOSE
            } else {
                CardListMode.LIST
            }
            val uiCards = filterCards(it.cards, mode, savedCardsOptions.showOnlyRecurrentCards)
            stateFlow.update {
                it.copy(
                    listState = if (uiCards.isEmpty()) {
                        CardsListState.Empty
                    } else {
                        CardsListState.Content(uiCards)
                    },
                    menuMode = resolveMenuMode(mode, uiCards)
                )
            }
        } catch (e: Exception) {
            handleGetCardListError(e)
        }
    }

    private fun filterCards(
        cards: Array<Card>,
        mode: CardListMode,
        showOnlyRecurrentCards: Boolean
    ): List<CardItemUiModel> {
        return cards
                .filter { card -> card.status == CardStatus.ACTIVE }
                .filter { card ->
                    if (showOnlyRecurrentCards) card.rebillId.isNullOrEmpty().not() else true
                }
            .map {
                val cardNumber = checkNotNull(it.pan)
                CardItemUiModel(
                    card = it,
                    bankName = bankCaptionProvider(cardNumber),
                    showChoose = (selectedCardIdFlow.value == it.cardId) && mode === CardListMode.CHOOSE
                )
            }
    }

    private fun handleGetCardListError(exception: Exception) {
        stateFlow.update {
            it.copy(
                listState = if (exception.checkCustomerNotFoundError()) {
                    CardsListState.Empty
                } else {
                    CardsListState.Error(exception)
                },
                menuMode = MenuMode.EMPTY
            )
        }
    }

    private fun handleDeleteCardSuccess(deletedCard: CardItemUiModel) {
        val currentListState = stateFlow.value.listState
        if (currentListState is CardsListState.Content) {
            val list = currentListState.cards.toMutableList()
            val indexAt = list.indexOfFirst { it.id == deletedCard.id }
            if (indexAt != -1) {
                list.removeAt(indexAt)
            }

            if (list.isEmpty()) {
                stateFlow.update {
                    it.copy(
                        listState = CardsListState.Empty,
                        menuMode = resolveMenuMode(it.listMode, null)
                    )
                }
                returnToBaseMode()
            } else {
                if (deletedCard.showChoose || deletedCard.id == selectedCardIdFlow.value) {
                    selectedCardIdFlow.value = list.firstOrNull()?.id
                }
                stateFlow.update {
                    it.copy(
                        listState = CardsListState.Content(list),
                        menuMode = resolveMenuMode(it.listMode, list)
                    )
                }
            }
        }

        eventFlow.value = CardListEvent.RemoveCardSuccess(deletedCard)
    }

    private fun payByNewCard() {
        navigationChannel.trySend(CardListNav.ToPayByNewCard())
    }

    private fun resolveMenuMode(mode: CardListMode, cards: List<CardItemUiModel>? = null): MenuMode {
        return when {
            cards.isNullOrEmpty() -> MenuMode.EMPTY
            (mode == CardListMode.LIST || mode == CardListMode.CHOOSE) -> MenuMode.EDIT
            mode == CardListMode.DELETE -> MenuMode.SUCCESS
            else -> MenuMode.EMPTY
        }
    }

    private fun resolveAllowNewCard(mode: CardListMode): Boolean {
        return when (mode) {
            CardListMode.DELETE -> false
            else -> savedCardsOptions.allowNewCard
        }
    }

    override fun onCleared() {
        manager.cancelAll()
        super.onCleared()
    }

    fun onReloadButtonClick() {
        loadData()
    }

    fun onRequestRefresh() {
        loadData()
    }

    companion object {
        fun factory(
            sdk: AcquiringSdk,
            connectionChecker: ConnectionChecker,
            bankCaptionProvider: BankCaptionProvider,
            manager: CoroutineManager = CoroutineManager()
        ) = viewModelFactory {
            initializer {
                CardsListFragmentViewModel(
                    createSavedStateHandle(),
                    sdk,
                    connectionChecker,
                    bankCaptionProvider,
                    manager
                )
            }
        }
    }
}
