package ru.tinkoff.acquiring.sdk.payment

import android.content.pm.PackageManager
import androidx.annotation.MainThread
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkException
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkTimeoutException
import ru.tinkoff.acquiring.sdk.models.NspkRequest
import ru.tinkoff.acquiring.sdk.models.enums.DataTypeQr
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.payment.base.PaymentUiEvent
import ru.tinkoff.acquiring.sdk.payment.methods.InitConfigurator.configure
import ru.tinkoff.acquiring.sdk.payment.methods.requiredPaymentId
import ru.tinkoff.acquiring.sdk.payment.pooling.GetStatusPooling
import ru.tinkoff.acquiring.sdk.redesign.sbp.util.NspkBankAppsProvider
import ru.tinkoff.acquiring.sdk.redesign.sbp.util.NspkInstalledAppsChecker
import ru.tinkoff.acquiring.sdk.redesign.sbp.util.SbpHelper

/**
 * Created by i.golovachev
 */
class SbpPaymentProcess internal constructor(
    private val sdk: AcquiringSdk,
    private val bankAppsProvider: NspkInstalledAppsChecker,
    private val nspkBankProvider: NspkBankAppsProvider,
    private val getStatusPooling: GetStatusPooling,
    private val scope: CoroutineScope
) {
    internal constructor(
        sdk: AcquiringSdk,
        bankAppsProvider: NspkInstalledAppsChecker,
        nspkBankAppsProvider: NspkBankAppsProvider,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(
        sdk,
        bankAppsProvider,
        nspkBankAppsProvider,
        GetStatusPooling(sdk),
        CoroutineScope(ioDispatcher)
    )

    val state = MutableStateFlow<SbpPaymentState>(SbpPaymentState.Created)
    private var looperJob: Job = Job()

    fun start(options: PaymentOptions) {
        scope.launch {
            runOrCatch {
                val nspkApps = nspkBankProvider.getNspkApps()
                val paymentId = options.paymentId ?: sendInit(options)
                state.value = SbpPaymentState.Started(paymentId)
                val deeplink = sendGetQr(paymentId)
                val installedApps = bankAppsProvider.checkInstalledApps(nspkApps, deeplink)
                state.value = SbpPaymentState.NeedChooseOnUi(paymentId, PaymentUiEvent.ShowApps(installedApps))
            }
        }
    }

    fun goingToBankApp() {
        when (val _state = state.value) {
            is SbpPaymentState.NeedChooseOnUi -> {
                state.value = SbpPaymentState.LeaveOnBankApp(_state.paymentId)
            }
            is SbpPaymentState.Stopped -> {
                state.value = SbpPaymentState.LeaveOnBankApp(_state.paymentId!!)
            }
            else -> Unit
        }
    }

    fun startCheckingStatus(retriesCount: Int? = null) {
        // выйдем из функции если стейт уже проверяется или вызов некорректен
        val _state = state.value
        if (_state is SbpPaymentState.LeaveOnBankApp) {
            state.value = SbpPaymentState.CheckingStatus(_state.paymentId, null)
            looperJob.cancel()
            looperJob = startLoping(retriesCount, paymentId = _state.paymentId)
        }
    }

    fun stop() {
        state.value = SbpPaymentState.Stopped(state.value.paymentId)
        if (looperJob.isActive) {
            looperJob.cancel()
        }
    }

    private suspend fun runOrCatch(block: suspend () -> Unit) = try {
        block()
    } catch (throwable: Throwable) {
        state.update {
            if (throwable is CancellationException) {
                SbpPaymentState.Stopped(it.paymentId)
            } else {
                SbpPaymentState.GetBankListFailed(it.paymentId, throwable)
            }
        }
    }

    private suspend fun sendInit(paymentOptions: PaymentOptions): Long {
        val response = sdk.init { configure(paymentOptions) }.execute()
        return response.requiredPaymentId()
    }

    private suspend fun sendGetQr(paymentId: Long): String = checkNotNull(
        sdk.getQr {
            this.paymentId = paymentId
            this.dataType = DataTypeQr.PAYLOAD
        }.execute().data,
    ) { "data from NSPK are null" }

    private fun startLoping(retriesCount: Int?, paymentId: Long): Job {
        return scope.launch {
            getStatusPooling.start(retriesCount = retriesCount, paymentId = paymentId)
                .map {
                    SbpPaymentState.mapResponseStatusToState(
                        status = it,
                        paymentId = paymentId
                    )
                }.catch {
                    emit(SbpPaymentState.PaymentFailed(throwable = it, paymentId = paymentId))
                }
                .collectLatest {
                    state.value = it
                }
        }
    }

    companion object {
        private var instance: SbpPaymentProcess? = null

        @MainThread
        internal fun init(
            sdk: AcquiringSdk,
            packageManager: PackageManager,
            bankAppsProvider: NspkInstalledAppsChecker = NspkInstalledAppsChecker { nspkBanks, dl ->
                SbpHelper.getBankApps(packageManager, dl, nspkBanks)
            },
            nspkBankAppsProvider: NspkBankAppsProvider = NspkBankAppsProvider {
                NspkRequest().execute().dictionary
            }
        ) {
            instance?.scope?.cancel()
            instance = SbpPaymentProcess(sdk, bankAppsProvider, nspkBankAppsProvider)
        }

        internal fun getRequired() = instance!!

        fun get() = instance
    }
}

sealed interface SbpPaymentState {
    val paymentId: Long?

    object Created : SbpPaymentState {
        override val paymentId: Long? = null
    }

    class Started(override val paymentId: Long) : SbpPaymentState

    class NeedChooseOnUi(
        override val paymentId: Long,
        val showApps: PaymentUiEvent.ShowApps
    ) : SbpPaymentState

    class GetBankListFailed(override val paymentId: Long?, val throwable: Throwable) :
        SbpPaymentState

    class LeaveOnBankApp(override val paymentId: Long) : SbpPaymentState

    class CheckingStatus(
        override val paymentId: Long,
        val status: ResponseStatus?
    ) : SbpPaymentState

    class PaymentFailed(override val paymentId: Long?, val throwable: Throwable) : SbpPaymentState

    class Success(override val paymentId: Long, val cardId: String?, val rebillId: String?) :
        SbpPaymentState

    class Stopped(override val paymentId: Long?) : SbpPaymentState

    companion object {

        fun mapResponseStatusToState(status: ResponseStatus, paymentId: Long) = when (status) {
            in ResponseStatus.successStatuses -> {
                Success(
                    paymentId, null, null
                )
            }
            in ResponseStatus.unhappyPassStatuses -> {
                PaymentFailed(
                    paymentId,
                    AcquiringSdkException(IllegalStateException("PaymentState = $status"))
                )
            }
            ResponseStatus.DEADLINE_EXPIRED -> {
                PaymentFailed(
                    paymentId,
                    AcquiringSdkTimeoutException(IllegalStateException("PaymentState = $status"))
                )
            }
            else -> CheckingStatus(paymentId, status)
        }
    }
}
