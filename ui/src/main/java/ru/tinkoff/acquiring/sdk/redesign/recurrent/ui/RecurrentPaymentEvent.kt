package ru.tinkoff.acquiring.sdk.redesign.recurrent.ui

import ru.tinkoff.acquiring.sdk.models.ThreeDsState
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions

sealed interface RecurrentPaymentEvent {

    class CloseWithError(val throwable: Throwable, val paymentId: Long?) : RecurrentPaymentEvent

    class CloseWithCancel(val paymentId: Long? = null) : RecurrentPaymentEvent

    class CloseWithSuccess(val paymentId: Long, val rebillId: String) : RecurrentPaymentEvent

    class To3ds(val paymentOptions: PaymentOptions, val threeDsState: ThreeDsState) : RecurrentPaymentEvent
}
