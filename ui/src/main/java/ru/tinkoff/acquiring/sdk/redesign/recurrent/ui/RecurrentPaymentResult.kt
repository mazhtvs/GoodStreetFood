package ru.tinkoff.acquiring.sdk.redesign.recurrent.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface RecurrentPaymentResult : Parcelable {

    @Parcelize
    data class CloseWithCancel(
        val paymentId: Long? = null
    ): RecurrentPaymentResult

    @Parcelize
    data class CloseWithSuccess(
        val paymentId: Long,
        val rebillId: String
    ) : RecurrentPaymentResult

    @Parcelize
    data class CloseWithError(
        val error: Throwable,
        val errorCode: Int? = null,
    ) : RecurrentPaymentResult
}