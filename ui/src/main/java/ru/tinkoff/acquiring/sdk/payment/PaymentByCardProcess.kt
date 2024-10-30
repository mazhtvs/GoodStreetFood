package ru.tinkoff.acquiring.sdk.payment

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringApiException
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkException
import ru.tinkoff.acquiring.sdk.exceptions.getPaymentIdIfSdkError
import ru.tinkoff.acquiring.sdk.models.ThreeDsState
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.paysources.CardSource
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.network.AcquiringApi
import ru.tinkoff.acquiring.sdk.payment.methods.Check3DsVersionMethods
import ru.tinkoff.acquiring.sdk.payment.methods.Check3DsVersionMethodsSdkImpl
import ru.tinkoff.acquiring.sdk.payment.methods.FinishAuthorizeMethods
import ru.tinkoff.acquiring.sdk.payment.methods.FinishAuthorizeMethodsSdkImpl
import ru.tinkoff.acquiring.sdk.payment.methods.InitMethods
import ru.tinkoff.acquiring.sdk.payment.methods.InitMethodsSdkImpl
import ru.tinkoff.acquiring.sdk.payment.methods.requiredPaymentId
import ru.tinkoff.acquiring.sdk.payment.pooling.GetStatusPooling
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsDataCollector
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper
import ru.tinkoff.acquiring.sdk.ui.activities.ThreeDsLauncher
import ru.tinkoff.acquiring.sdk.utils.CoroutineManager

/**
 * Created by i.golovachev
 */
class PaymentByCardProcess internal constructor(
    private val initMethods: InitMethods,
    private val check3DsVersionMethods: Check3DsVersionMethods,
    private val finishAuthorizeMethods: FinishAuthorizeMethods,
    private val coroutineManager: CoroutineManager = CoroutineManager(),
    private val getStatusPooling: GetStatusPooling
) {

    private lateinit var paymentSource: CardSource
    private val _state = MutableStateFlow<PaymentByCardState>(PaymentByCardState.Created)
    val state = _state.asStateFlow()

    fun start(
        cardData: CardSource,
        paymentOptions: PaymentOptions,
        email: String? = null
    ) {
        _state.value = PaymentByCardState.Started(paymentOptions, email)
        coroutineManager.launchOnBackground {
            try {
                startFlow(cardData, paymentOptions, email)
            } catch (e: Throwable) {
                handleException(e)
            }
        }
    }

    fun goTo3ds() {
        _state.value = PaymentByCardState.ThreeDsInProcess
    }

    fun stop() {
        coroutineManager.cancelAll()
    }

    internal fun set3dsResult(paymentResult: PaymentResult) {
        _state.value =
            PaymentByCardState.Success(
                paymentResult.paymentId ?: 0,
                paymentResult.cardId,
                paymentResult.rebillId
            )
    }

    fun set3dsResult(error: ThreeDsLauncher.Result.Error?) {
        _state.value =
            PaymentByCardState.Error(error?.error ?: AcquiringSdkException(IllegalStateException()), paymentId = error?.paymentId)
    }

    fun recreate() {
        _state.value = PaymentByCardState.Created
    }

    private suspend fun startFlow(
        card: CardSource,
        options: PaymentOptions,
        email: String?,
    ) {
        this.paymentSource = card

        val paymentId = options.paymentId
            ?: initMethods
                .init(options, email)
                .requiredPaymentId()

        val data3ds = check3DsVersionMethods.callCheck3DsVersion(
            paymentId = paymentId,
            paymentSource = card,
            paymentOptions = options,
            email = email
        )

        val finish = finishAuthorizeMethods.finish(
            paymentId = paymentId,
            paymentSource = card,
            paymentOptions = options,
            email = email,
            data = data3ds.additionalData,
            threeDsVersion = data3ds.threeDsVersion,
            threeDsTransaction = data3ds.threeDsTransaction
        )

        when (finish) {
            is FinishAuthorizeMethods.Result.Need3ds -> _state.value =
                PaymentByCardState.ThreeDsUiNeeded(
                    threeDsState = finish.threeDsState,
                    paymentOptions = options
                )

            is FinishAuthorizeMethods.Result.Success -> _state.value = PaymentByCardState.Success(
                paymentId = finish.paymentId,
                cardId = finish.cardId,
                rebillId = finish.rebillId
            )

            is FinishAuthorizeMethods.Result.NeedGetState -> {
                coroutineManager.launchOnBackground {
                    getStatusPooling.start(paymentId = finish.paymentId)
                        .map {
                            PaymentByCardState.mapResponseStatusToState(
                                status = it,
                                paymentId = paymentId,
                                paymentOptions = options
                            )
                        }
                        .catch {
                            emit(
                                PaymentByCardState.Error(
                                    throwable = it,
                                    paymentId = paymentId
                                )
                            )
                        }
                        .collectLatest {
                            _state.value = it
                        }
                }
            }
        }
    }

    private fun handleException(throwable: Throwable) {
        if (throwable is AcquiringApiException && throwable.response != null &&
            throwable.response!!.errorCode == AcquiringApi.API_ERROR_CODE_3DSV2_NOT_SUPPORTED
        ) {
            // todo
        } else {
            val paymentId = throwable.getPaymentIdIfSdkError()
            _state.update { PaymentByCardState.Error(throwable, paymentId) }
        }
    }

    companion object {

        private var value: PaymentByCardProcess? = null

        fun get() = value!!

        @Synchronized
        fun init(
            sdk: AcquiringSdk,
            application: Application,
            threeDsDataCollector: ThreeDsDataCollector = ThreeDsHelper.CollectData
        ) {
            value = PaymentByCardProcess(
                InitMethodsSdkImpl(sdk),
                Check3DsVersionMethodsSdkImpl(sdk, application, threeDsDataCollector),
                FinishAuthorizeMethodsSdkImpl(sdk),
                CoroutineManager(),
                GetStatusPooling(sdk)
            )
        }
    }
}

// кажется, проще будет привести все состояния к одному типу.db
sealed interface PaymentByCardState {
    object Created : PaymentByCardState

    class Started(
        val paymentOptions: PaymentOptions,
        val email: String? = null,
        val paymentId: Long? = null
    ) : PaymentByCardState

    class ThreeDsUiNeeded(val threeDsState: ThreeDsState, val paymentOptions: PaymentOptions) :
        PaymentByCardState

    object ThreeDsInProcess : PaymentByCardState

    class CvcUiNeeded(val paymentOptions: PaymentOptions, val rejectedPaymentId: String) :
        PaymentByCardState

    object CvcUiInProcess : PaymentByCardState

    class Success(val paymentId: Long, val cardId: String?, val rebillId: String?) :
        PaymentByCardState {
        internal val result = PaymentResult(paymentId, cardId, rebillId)
    }

    class Error(val throwable: Throwable, val paymentId: Long?) : PaymentByCardState

    companion object {

        fun mapResponseStatusToState(
            status: ResponseStatus,
            paymentId: Long,
            paymentOptions: PaymentOptions
        ): PaymentByCardState =
            when (status) {
                in ResponseStatus.successStatuses -> {
                    Success(
                        paymentId, null, null
                    )
                }

                in ResponseStatus.processStatuses -> {
                    Started(paymentOptions, paymentOptions.customer.email)
                }

                else -> {
                    Error(
                        AcquiringSdkException(
                            IllegalStateException("PaymentState = $status"),
                        ),
                        paymentId
                    )
                }
            }
    }
}
