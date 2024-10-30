package ru.tinkoff.acquiring.sdk.redesign.recurrent.presentation

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.paysources.AttachedCard
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.payment.PaymentByCardState
import ru.tinkoff.acquiring.sdk.payment.RecurrentPaymentProcess
import ru.tinkoff.acquiring.sdk.redesign.dialog.PaymentStatusSheetState
import ru.tinkoff.acquiring.sdk.redesign.recurrent.nav.RecurrentPaymentNavigation
import ru.tinkoff.acquiring.sdk.redesign.recurrent.ui.RecurrentPaymentActivity.Companion.EXTRA_CARD
import ru.tinkoff.acquiring.sdk.redesign.recurrent.ui.RecurrentPaymentEvent
import ru.tinkoff.acquiring.sdk.redesign.recurrent.ui.RecurrentPaymentFragment.Companion.ARG_RECURRENT_PAYMENT_OPTION
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsDataCollector
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper
import ru.tinkoff.acquiring.sdk.utils.BankCaptionResourceProvider
import ru.tinkoff.acquiring.sdk.utils.CoroutineManager

internal class RecurrentPaymentViewModel(
    private val recurrentPaymentProcess: RecurrentPaymentProcess,
    private val recurrentProcessMapper: RecurrentProcessMapper,
    private val savedStateHandle: SavedStateHandle,
    private val recurrentPaymentNavigation: RecurrentPaymentNavigation.Impl,
) : ViewModel() , RecurrentPaymentNavigation by recurrentPaymentNavigation  {

    // started
    private val paymentOptions = checkNotNull(savedStateHandle.get<PaymentOptions>(ARG_RECURRENT_PAYMENT_OPTION))
    private val card = checkNotNull(savedStateHandle.get<Card>(EXTRA_CARD))
    // state
    val state: Flow<PaymentStatusSheetState?> = recurrentPaymentProcess.state.map {
        recurrentProcessMapper(it)
    }

    fun pay() {
        viewModelScope.launch {
            recurrentPaymentProcess.start(
                AttachedCard(card.rebillId),
                paymentOptions,
                paymentOptions.customer.email,
            )
        }
    }

    fun set3dsResult(error: Throwable?) {
        recurrentPaymentProcess.set3dsResult(error)
    }

    fun set3dsResult(paymentResult: PaymentResult) {
        recurrentPaymentProcess.set3dsResult(paymentResult)
    }

    fun goTo3ds() {
        recurrentPaymentProcess.onThreeDsUiInProcess()
    }

    fun onClose() {
        viewModelScope.launch {
            when (val paymentState = recurrentPaymentProcess.state.value) {
                PaymentByCardState.Created -> Unit
                is PaymentByCardState.CvcUiNeeded -> recurrentPaymentNavigation.eventChannel.send(RecurrentPaymentEvent.CloseWithCancel())
                is PaymentByCardState.Error -> recurrentPaymentNavigation.eventChannel.send(
                    RecurrentPaymentEvent.CloseWithError(
                        throwable = paymentState.throwable,
                        paymentId = paymentState.paymentId
                    )
                )
                is PaymentByCardState.Started -> Unit
                is PaymentByCardState.Success -> recurrentPaymentNavigation.eventChannel.send(
                    RecurrentPaymentEvent.CloseWithSuccess(
                        paymentId = paymentState.paymentId,
                        rebillId = requireNotNull(paymentState.rebillId) { "Не указан rebillId для рекуррентного платежа" },
                    )
                )
                PaymentByCardState.ThreeDsInProcess ->  recurrentPaymentNavigation.eventChannel.send(
                    RecurrentPaymentEvent.CloseWithCancel()
                )
                is PaymentByCardState.ThreeDsUiNeeded ->  recurrentPaymentNavigation.eventChannel.send(
                    RecurrentPaymentEvent.CloseWithCancel()
                )
                else -> Unit
            }
        }
    }
}

fun RecurrentViewModelsFactory(
    application: Application,
    paymentOptions: PaymentOptions,
    threeDsDataCollector: ThreeDsDataCollector = ThreeDsHelper.CollectData
)  =  viewModelFactory {
    RecurrentPaymentProcess.init(
        TinkoffAcquiring(
            application,
            paymentOptions.terminalKey,
            paymentOptions.publicKey
        ).sdk,
        application,
        threeDsDataCollector
    )
    val recurrentPaymentNavigation = RecurrentPaymentNavigation.Impl()
    val recurrentProcessMapper = RecurrentProcessMapper(recurrentPaymentNavigation)
    val bankCaptionResourceProvider = BankCaptionResourceProvider(application)
    val coroutineManager = CoroutineManager()
    initializer {
        RecurrentPaymentViewModel(
            RecurrentPaymentProcess.get(),
            recurrentProcessMapper,
            createSavedStateHandle(),
            recurrentPaymentNavigation,
        )
    }
    initializer {
        RejectedViewModel(
            RecurrentPaymentProcess.get(),
            bankCaptionResourceProvider,
            coroutineManager,
            createSavedStateHandle()
        )
    }
}
