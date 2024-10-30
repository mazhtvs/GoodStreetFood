package ru.tinkoff.acquiring.sdk.redesign.mainform.presentation

import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.enums.CardStatus
import ru.tinkoff.acquiring.sdk.redesign.common.savedcard.SavedCardsRepository
import ru.tinkoff.acquiring.sdk.redesign.mainform.presentation.primary.PrimaryButtonConfigurator
import ru.tinkoff.acquiring.sdk.redesign.mainform.presentation.secondary.SecondButtonConfigurator
import ru.tinkoff.acquiring.sdk.redesign.payment.model.CardChosenModel
import ru.tinkoff.acquiring.sdk.responses.TerminalInfo
import ru.tinkoff.acquiring.sdk.utils.BankCaptionProvider
import ru.tinkoff.acquiring.sdk.utils.ConnectionChecker
import ru.tinkoff.acquiring.sdk.utils.checkNotNull

/**
 * Created by i.golovachev
 */
internal class MainPaymentFormFactory(
    private val sdk: AcquiringSdk,
    private val savedCardsRepository: SavedCardsRepository,
    private val primaryButtonConfigurator: PrimaryButtonConfigurator,
    private val secondButtonConfigurator: SecondButtonConfigurator,
    private val mergeMethodsStrategy: MergeMethodsStrategy,
    private val connectionChecker: ConnectionChecker,
    private val bankCaptionProvider: BankCaptionProvider,
    private val customerKey: String? = null
) {

    suspend fun getState(): MainPaymentForm.State {
        val terminalInfo = getTerminalInfo()
        val cards = getSavedCards(customerKey)
        return getUi(terminalInfo, cards)
    }

    suspend fun getUi(terminalInfo: TerminalInfo?, cards: List<Card>): MainPaymentForm.State {
        val primary = primaryButtonConfigurator.get(terminalInfo, cards)
        val secondaries = secondButtonConfigurator.get(terminalInfo, cards)

        return MainPaymentForm.State(
            ui = mergeMethodsStrategy.merge(primary, secondaries),
            data = MainPaymentForm.Data(
                terminalInfo,
                cards,
                cards.firstOrNull()
            ),
            noInternet = connectionChecker.isOnline().not()
        )
    }

    fun changeCard(state: MainPaymentForm.State, card: Card): MainPaymentForm.State {
        val choosenCard = CardChosenModel(card, bankCaptionProvider.invoke(card.pan!!))
        return MainPaymentForm.State(
            ui = state.ui.copy(primary = MainPaymentForm.Primary.Card(choosenCard)),
            data = state.data.copy(chosen = card),
            noInternet = connectionChecker.isOnline().not()
        )
    }

    private suspend fun getSavedCards(customerKey: String?): List<Card> {
        if (customerKey == null) return emptyList()

        return try {
            savedCardsRepository.getCards(customerKey, true)
                .filter { it.status == CardStatus.ACTIVE }
        } catch (e: Throwable) {
            AcquiringSdk.log(e)
            emptyList()
        }
    }

    private suspend fun getTerminalInfo() : TerminalInfo {
        return sdk.getTerminalPayMethods().execute()
            .terminalInfo
            .checkNotNull { "Не удалось получить информацию о доступных способах оплаты" }
    }
}
