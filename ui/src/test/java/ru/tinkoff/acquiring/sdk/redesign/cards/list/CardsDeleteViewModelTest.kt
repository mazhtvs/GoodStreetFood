package ru.tinkoff.acquiring.sdk.redesign.cards.list

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import common.MutableCollector
import common.assertByClassName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.redesign.cards.list.models.CardItemUiModel
import ru.tinkoff.acquiring.sdk.redesign.cards.list.presentation.CardsListFragmentViewModel
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardListEvent
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardsListFragment
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardsListState
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.ScreenState
import ru.tinkoff.acquiring.sdk.requests.RemoveCardRequest
import ru.tinkoff.acquiring.sdk.responses.RemoveCardResponse
import ru.tinkoff.acquiring.sdk.utils.BankCaptionProvider
import ru.tinkoff.acquiring.sdk.utils.ConnectionChecker
import ru.tinkoff.acquiring.sdk.utils.CoroutineManager
import ru.tinkoff.acquiring.sdk.utils.RequestResult
import java.util.concurrent.Executors

/**
 * Created by Ivan Golovachev
 */
internal class CardsDeleteViewModelTest {

    val defaultContent = CardsListState.Content(
            listOf(
                    createCard("1"),
                    createCard("2")
            )
    )

    val extendsContent = CardsListState.Content(
            listOf(
                    createCard("1"),
                    createCard("2"),
                    createCard("3")
            )
    )

    @Test
    fun `when card delete complete`() = runBlocking {
        with(Environment(initState = defaultContent, key = "1")) {
            eventCollector.takeValues(2)
            setResponse(RequestResult.Success(RemoveCardResponse(1)))

            val card = createCard("1")
            vm.deleteCard(card)

            eventCollector.joinWithTimeout()
            eventCollector.flow.test {
                assertByClassName(CardListEvent.RemoveCardProgress(mock()), awaitItem())
                assertByClassName(CardListEvent.RemoveCardSuccess(card), awaitItem())
                awaitComplete()
            }
        }
    }

    @Test
    fun `when card delete throw error`() = runBlocking {
        with(Environment(initState = defaultContent, key = "")) {

            eventCollector.takeValues(2)
            setResponse(RequestResult.Failure(Exception()))

            vm.deleteCard(createCard("1"))

            eventCollector.joinWithTimeout()
            eventCollector.flow.test {
                assertByClassName(CardListEvent.RemoveCardProgress(mock()), awaitItem())
                assertByClassName(CardListEvent.ShowCardDeleteError(mock()), awaitItem())
                awaitComplete()
            }
        }
    }


    @Test
    fun `when card delete without key`() = runBlocking {
        with(Environment(initState = defaultContent)) {

            eventCollector.takeValues(2)
            setResponse(RequestResult.Failure(Exception()))

            vm.deleteCard(createCard("1"))

            eventCollector.joinWithTimeout()
            eventCollector.flow.test {
                awaitItem()
                assertByClassName(CardListEvent.ShowError, awaitItem())
                awaitComplete()
            }
        }
    }

    @Test
    fun `when card delete is offline`() = runBlocking {
        with(Environment(initState = defaultContent)) {

            eventCollector.takeValues(2)
            setResponse(RequestResult.Failure(Exception()))
            setOnline(false)

            vm.deleteCard(createCard("1"))
            eventCollector.joinWithTimeout()
            eventCollector.flow.test {
                awaitItem()
                assertByClassName(CardListEvent.ShowError, awaitItem())
                awaitComplete()
            }
        }
    }

    @Test
    fun `when card delete multiply show last card`() = runBlocking {
        with(Environment(initState = extendsContent, key = "")) {

            stateCollector.takeValues(2)

            setResponse(RequestResult.Success(RemoveCardResponse(1)))
            vm.deleteCard(createCard("1"))

            setResponse(RequestResult.Success(RemoveCardResponse(2)))
            vm.deleteCard(createCard("2"))

            stateCollector.joinWithTimeout()
            stateCollector.flow.test {
                assertByClassName(CardsListState.Content::class.java, awaitItem().javaClass)
                assertByClassName(CardsListState.Content::class.java, awaitItem().javaClass)
                awaitComplete()
            }
        }
    }


    class Environment(
        initState: CardsListState,
        val connectionMock: ConnectionChecker = mock { on { isOnline() } doReturn true },
        val asdk: AcquiringSdk = mock { },
        savedStateHandle: SavedStateHandle? = null,
        key: String? = null,
    ) {
        val savedCardsOptions = SavedCardsOptions().apply {
            customerOptions { customerKey = key }
        }
        val defaultStateHandle = mock<SavedStateHandle> {
            on { get<SavedCardsOptions>(eq(CardsListFragment.ARG_SAVED_CARDS_OPTION)) } doReturn savedCardsOptions
        }

        val dispatcher: CoroutineDispatcher =
            Executors.newSingleThreadExecutor().asCoroutineDispatcher()

        val vm = CardsListFragmentViewModel(
            savedStateHandle = savedStateHandle ?: defaultStateHandle,
            asdk,
            connectionMock,
            BankCaptionProvider { "Tinkoff" },
            CoroutineManager(dispatcher, dispatcher)
        ).apply {
            stateFlow.value =  stateFlow.value.copy(
                    listState = initState
            )
        }

        val eventCollector = MutableCollector<CardListEvent>(vm.eventFlow)

        val stateCollector = MutableCollector<ScreenState>(vm.stateFlow)

        fun setOnline(isOnline: Boolean) {
            whenever(connectionMock.isOnline()).doReturn(isOnline)
        }

        fun setResponse(response: RequestResult<out RemoveCardResponse>) {
            val request: RemoveCardRequest =
                mock { on { executeFlow() } doReturn MutableStateFlow(response) }

            whenever(asdk.removeCard(any())).doReturn(request)
        }

    }

    private fun createCard(idMock: String): CardItemUiModel = mock { on { id } doReturn idMock }
}
