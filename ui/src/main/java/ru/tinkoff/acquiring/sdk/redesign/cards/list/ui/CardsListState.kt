package ru.tinkoff.acquiring.sdk.redesign.cards.list.ui

import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.redesign.cards.list.models.CardItemUiModel

/**
 * Created by Ivan Golovachev
 */
data class ScreenState(
    val screenMode: ScreenMode,
    val listMode: CardListMode,
    val listState: CardsListState,
    val withArrowBack: Boolean = false,
    val allowNewCard: Boolean = true,
    val menuMode: MenuMode = MenuMode.EMPTY
) {
    companion object {
        val DEFAULT = ScreenState(
            screenMode = ScreenMode.LIST,
            listMode = CardListMode.LIST,
            listState = CardsListState.Shimmer
        )
    }
}

sealed class CardsListState {
    object Shimmer : CardsListState()
    object Empty : CardsListState()
    class Error(val throwable: Throwable) : CardsListState()
    object NoNetwork : CardsListState()
    class Content(val cards: List<CardItemUiModel>) : CardsListState()
}

sealed class CardListEvent {
    class RemoveCardProgress(
        val deletedCard: CardItemUiModel
    ) : CardListEvent()

    class RemoveCardSuccess(
        val deletedCard: CardItemUiModel
    ) : CardListEvent()

    object ShowError : CardListEvent()

    class ShowCardDeleteError(val it: Throwable) : CardListEvent()

    class ShowCardAttachDialog(val it: String) : CardListEvent()
}


sealed class CardListNav {

    class ToAttachCard() : CardListNav()

    class ToPayByNewCard() : CardListNav()

    class FinishWithError(
        val throwable: Throwable
    ) : CardListNav()

    class FinishWithSelectCard(val selectedCard: Card) : CardListNav()

    object FinishWithCancel : CardListNav()
}

enum class ScreenMode {
    /**
     *  Отображение списка карт
     */
    LIST,
    /**
     * Выбор карты для оплаты
     */
    PAYMENT
}

enum class CardListMode {
    LIST, DELETE, CHOOSE
}

enum class MenuMode {
    /**
     * Действия отсутвтвуют
     */
    EMPTY,
    /**
     * Доступно редактирование
     */
    EDIT,
    /**
     * Доступно завершение редактирования
     */
    SUCCESS
}
