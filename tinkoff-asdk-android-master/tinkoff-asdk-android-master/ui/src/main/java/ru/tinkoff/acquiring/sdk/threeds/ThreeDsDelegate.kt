package ru.tinkoff.acquiring.sdk.threeds

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkException
import ru.tinkoff.acquiring.sdk.models.ThreeDsData
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus
import ru.tinkoff.acquiring.sdk.models.result.AsdkResult
import ru.tinkoff.acquiring.sdk.models.result.CardResult
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.payment.pooling.GetStatusPooling
import ru.tinkoff.acquiring.sdk.utils.CoroutineManager

internal interface ThreeDsDelegate {

    fun initDelegate(coroutine: CoroutineManager)

    fun submitAuthorization(threeDsData: ThreeDsData, transStatus: String)

    val threeDsState: StateFlow<ThreeDsState>

    val resultLiveData: LiveData<AsdkResult>

    fun checkoutTransactionStatus()

    class Impl(private val sdk: AcquiringSdk): ThreeDsDelegate {

        private var coroutine: CoroutineManager? = null

        override val threeDsState = MutableStateFlow<ThreeDsState>(ThreeDsState.Empty)

        private var requestPaymentStateJob: Job? = null

        private val getStatusPooling = GetStatusPooling(sdk)

        private val asdkResult: MutableLiveData<AsdkResult> = MutableLiveData()

        override val resultLiveData: LiveData<AsdkResult> = asdkResult

        override fun checkoutTransactionStatus(){
            ThreeDsHelper.checkoutTransactionStatus { status ->

                when (status) {
                    is ThreeDsStatusSuccess -> submitAuthorization(status.threeDsData, status.transStatus)
                    is ThreeDsStatusCanceled -> {
                        threeDsState.value = ThreeDsState.FinishWithCancel
                    }
                    is ThreeDsStatusError -> {
                        ThreeDsState.FinishWithError(status.error)
                    }
                    else -> Unit
                }
            }
        }

        override fun initDelegate(coroutine: CoroutineManager) {
            this.coroutine = coroutine
        }

        override fun submitAuthorization(threeDsData: ThreeDsData, transStatus: String) {
            threeDsState.value = ThreeDsState.LoadingState

            val request = sdk.submit3DSAuthorization(threeDsData.tdsServerTransId!!, transStatus)

            coroutine?.call(request,
                onSuccess = {
                    requestState(threeDsData)
                }, onFailure = {
                    requestState(threeDsData)
                })
        }

        private fun requestState(threeDsData: ThreeDsData) {
            if (threeDsData.isPayment) {
                requestPaymentState(threeDsData.paymentId)
            } else if (threeDsData.isAttaching) {
                requestAddCardState(threeDsData.requestKey)
            }
        }

        private fun requestPaymentState(paymentId: Long?) {
            requestPaymentStateJob?.cancel()
            threeDsState.value = ThreeDsState.LoadingState
            requestPaymentStateJob = coroutine?.launchOnMain {
                getStatusPooling.start(paymentId = paymentId!!)
                    .flowOn(Dispatchers.IO)
                    .catch {
                        threeDsState.value = ThreeDsState.ErrorState(it, paymentId)
                    }
                    .filter { ResponseStatus.checkSuccessStatuses(it) }
                    .collect { handleConfirmOnAuthStatus(paymentId) }
            }
        }

        private fun requestAddCardState(requestKey: String?) {
            threeDsState.value = ThreeDsState.LoadingState

            val request = sdk.getAddCardState {
                this.requestKey = requestKey
            }

            coroutine?.call(request,
                onSuccess = { response ->
                    if (response.status == ResponseStatus.COMPLETED) {
                        asdkResult.value = CardResult(response.cardId, null)
                    } else {
                        val throwable =
                            AcquiringSdkException(IllegalStateException("AsdkState = ${response.status}"))
                        threeDsState.value = ThreeDsState.ErrorState(throwable)
                    }
                    threeDsState.value = ThreeDsState.LoadedState
                })
        }

        private fun handleConfirmOnAuthStatus(paymentId: Long) {
            asdkResult.postValue(PaymentResult(paymentId))
            threeDsState.value = ThreeDsState.LoadedState
        }
    }

}

sealed interface ThreeDsState {

    object Empty : ThreeDsState
    object LoadingState : ThreeDsState
    object LoadedState : ThreeDsState
    object FinishWithCancel : ThreeDsState
    class FinishWithError(val throwable: Throwable) : ThreeDsState
    class ErrorState(val throwable: Throwable, val paymentId: Long? = null) : ThreeDsState
}
