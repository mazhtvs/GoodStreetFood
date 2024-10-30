package ru.tinkoff.acquiring.sdk.redesign.cards.list

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import common.AcquiringResponseStub
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringApiException
import ru.tinkoff.acquiring.sdk.models.enums.CardStatus
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.network.AcquiringApi.API_ERROR_CODE_CUSTOMER_NOT_FOUND
import ru.tinkoff.acquiring.sdk.redesign.cards.list.presentation.CardsListFragmentViewModel
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardsListFragment
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardsListState
import ru.tinkoff.acquiring.sdk.requests.GetCardListRequest
import ru.tinkoff.acquiring.sdk.responses.GetCardListResponse
import ru.tinkoff.acquiring.sdk.utils.BankCaptionProvider
import ru.tinkoff.acquiring.sdk.utils.ConnectionChecker
import ru.tinkoff.acquiring.sdk.utils.RequestResult
import turbineDelay

/**
 * Created by Ivan Golovachev
 */
internal class CardsListFragmentViewModelTest {

    @Test
    fun `when customer key are null`() {
        runViewModelCardsLoadTest<CardsListState.Error>(
            null,
            RequestResult.Success(
                GetCardListResponse(
                    emptyArray()
                )
            )
        )
    }

    @Test
    fun `when cards response return error`() {
        runViewModelCardsLoadTest<CardsListState.Error>(
            null,
            RequestResult.Failure(Exception())
        )
    }

//    @Test
//    fun `when card list empty`() {
//        runViewModelCardsLoadTest<CardsListState.Empty>(
//            "key",
//            RequestResult.Success(
//                GetCardListResponse(
//                    emptyArray()
//                )
//            )
//        )
//    }

    @Test
    fun `when card list full`() {
        runViewModelCardsLoadTest<CardsListState.Content>(
            "key",
            RequestResult.Success(
                GetCardListResponse(
                    arrayOf(mock {
                        on { pan } doReturn "3413413413413414"
                        on { cardId } doReturn "1"
                        on { status } doReturn CardStatus.ACTIVE
                    })
                )
            )
        )
    }

    @Test
    fun `when card list only D status full`() {
        runViewModelCardsLoadTest<CardsListState.Empty>(
            "key",
            RequestResult.Success(
                GetCardListResponse(
                    arrayOf(mock {
                        on { pan } doReturn "3413413413413414"
                        on { cardId } doReturn "1"
                        on { status } doReturn CardStatus.DELETED
                    })
                )
            )
        )
    }

    @Test
    fun `when connection lost`() {
        runViewModelCardsLoadTest<CardsListState.NoNetwork>(
            "key",
            RequestResult.Success(
                GetCardListResponse(
                    arrayOf(mock {
                        on { pan } doReturn "3413413413413414"
                        on { cardId } doReturn "1"
                        on { status } doReturn CardStatus.DELETED
                    })
                )
            ),
            connectionChecker = mock { on { isOnline() } doReturn false }
        )
    }

    @Test
    fun `when API_ERROR_CODE_CUSTOMER_NOT_FOUND then empty state`() {
        runViewModelCardsLoadTest<CardsListState.Empty>(
            "key",
            RequestResult.Failure(AcquiringApiException(AcquiringResponseStub(API_ERROR_CODE_CUSTOMER_NOT_FOUND))),
            connectionChecker = mock { on { isOnline() } doReturn true }
        )
    }

    private inline fun <reified T : CardsListState> runViewModelCardsLoadTest(
        key: String?,
        requestResult: RequestResult<out GetCardListResponse>,
        connectionChecker: ConnectionChecker = mock {
            on { isOnline() } doReturn true
        }
    ) {
        runBlocking {
            val viewModel = createViewModelMock(requestResult, connectionChecker, key)
            turbineDelay()
            viewModel.stateUiFlow.map { it.listState }.test {
                val awaitNextEvent = awaitItem()
                val excClass = T::class
                assertTrue(
                    "${awaitNextEvent.javaClass.simpleName} is not ${excClass.simpleName}",
                    awaitNextEvent::class == excClass
                )
            }
        }
    }

    private fun createViewModelMock(
        result: RequestResult<out GetCardListResponse>,
        connectionChecker: ConnectionChecker,
        key: String?
    ): CardsListFragmentViewModel {
        val request = mock<GetCardListRequest> {
            on { executeFlow() } doReturn MutableStateFlow(result)
        }
        val sdk = mock<AcquiringSdk> {
            on { getCardList(any()) } doReturn request
        }
        val provider = BankCaptionProvider {
            "Tinkoff"
        }
        val savedCardsOptions = SavedCardsOptions().apply {
            customerOptions { customerKey = key }
        }
        val savedStateHandle = mock<SavedStateHandle> {
            on { get<SavedCardsOptions>(eq(CardsListFragment.ARG_SAVED_CARDS_OPTION)) } doReturn savedCardsOptions
        }
        return CardsListFragmentViewModel(savedStateHandle,sdk, connectionChecker, provider)
    }

}
