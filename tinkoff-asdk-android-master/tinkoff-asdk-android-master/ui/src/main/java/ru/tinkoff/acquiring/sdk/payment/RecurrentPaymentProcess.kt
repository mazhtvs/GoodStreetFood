package ru.tinkoff.acquiring.sdk.payment

import android.app.Application
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringApiException
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkException
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.paysources.AttachedCard
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.network.AcquiringApi
import ru.tinkoff.acquiring.sdk.payment.methods.ChargeMethods
import ru.tinkoff.acquiring.sdk.payment.methods.ChargeMethodsSdkImpl
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
import ru.tinkoff.acquiring.sdk.utils.CoroutineManager

/**
 * Created by i.golovachev
 */
class RecurrentPaymentProcess internal constructor(
    private val initMethods: InitMethods,
    private val chargeMethods: ChargeMethods,
    private val check3DsVersionMethods: Check3DsVersionMethods,
    private val finishAuthorizeMethods: FinishAuthorizeMethods,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
    private val coroutineManager: CoroutineManager = CoroutineManager(), // TODO нужны только диспы отсюда
    private val getStatusPooling: GetStatusPooling,
) {

    private val _state = MutableStateFlow<PaymentByCardState>(PaymentByCardState.Created)
    private val _paymentIdOrNull get() = (_state.value as? PaymentByCardState.Started)?.paymentId // TODO после слияния состояний
    private val rejectedPaymentId = MutableStateFlow<String?>(null)
    val state = _state.asStateFlow()

    fun start(
        cardData: AttachedCard,
        paymentOptions: PaymentOptions,
        email: String? = null
    ) {
        scope.launch(coroutineManager.io) {
            try {
                startPaymentFlow(cardData, paymentOptions, email)
            } catch (e: Throwable) {
                handleException(paymentOptions, e, _paymentIdOrNull, true)
            }
        }
    }

    fun startWithCvc(
        cvc: String,
        rebillId: String,
        rejectedId: String,
        paymentOptions: PaymentOptions,
        email: String?
    ) {
        check(_state.value is PaymentByCardState.CvcUiNeeded)
        scope.launch(coroutineManager.io) {
            try {
                startRejectedFlow(cvc, rebillId, rejectedId, paymentOptions, email)
            } catch (e: Throwable) {
                handleException(paymentOptions, e, _paymentIdOrNull, false)
            }
        }
    }

    fun recreate() {
        _state.value = PaymentByCardState.Created
    }

    fun onThreeDsUiInProcess() {
        _state.value = PaymentByCardState.ThreeDsInProcess
    }

    internal fun set3dsResult(paymentResult: PaymentResult) {
        _state.value =
            PaymentByCardState.Success(
                paymentResult.paymentId ?: 0,
                paymentResult.cardId,
                paymentResult.rebillId
            )
    }

    fun set3dsResult(paymentId: Long?, cardId: String?, rebillId: String) {
        _state.value =
            PaymentByCardState.Success(
                paymentId ?: 0,
                cardId,
                rebillId
            )
    }

    fun set3dsResult(error: Throwable?) {
        _state.value =
            PaymentByCardState.Error(error ?: AcquiringSdkException(IllegalStateException()), null)
    }

    private suspend fun startPaymentFlow(
        cardData: AttachedCard,
        paymentOptions: PaymentOptions,
        email: String?
    ) {
        val paymentId = paymentOptions.paymentId
            ?: initMethods
                .init(paymentOptions, email)
                .requiredPaymentId()

        _state.value = PaymentByCardState.Started(
            paymentOptions = paymentOptions,
            email = email,
            paymentId = paymentId
        )

        val result = chargeMethods
            .charge(paymentId = paymentId, rebillId = cardData.rebillId)

        when (result) {
            is ChargeMethods.Result.Success -> _state.value = PaymentByCardState.Success(
                paymentId = result.paymentId,
                cardId = result.cardId,
                rebillId = result.rebillId
            )
            is ChargeMethods.Result.Failure -> _state.value = PaymentByCardState.Error(
                throwable = result.failure,
                paymentId = paymentId
            )
            is ChargeMethods.Result.NeedGetState -> {
                startGetStatePolling(result.paymentId, paymentId, paymentOptions)
            }
        }
    }

    private fun startGetStatePolling(resultPaymentId: Long, paymentId: Long, paymentOptions: PaymentOptions) {
        coroutineManager.launchOnBackground {
            getStatusPooling.start(paymentId = resultPaymentId)
                .map {
                    PaymentByCardState.mapResponseStatusToState(
                        status = it,
                        paymentId = paymentId,
                        paymentOptions = paymentOptions
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

    private suspend fun startRejectedFlow(
        cvc: String,
        rebillId: String,
        rejectedId: String,
        paymentOptions: PaymentOptions,
        email: String?
    ) {
        _state.value = PaymentByCardState.CvcUiInProcess

        val cardId = chargeMethods
            .getCardByRebillId(
                rebillId = rebillId,
                paymentOptions = paymentOptions
            )
            .cardId

        val card = AttachedCard(cardId = cardId, cvc = cvc)

        val paymentId = paymentOptions.paymentId
            ?: chargeMethods
                .init(
                    paymentOptions = paymentOptions,
                    email = email,
                    rejectedPaymentId = rejectedId
                )
                .requiredPaymentId()

        _state.value = PaymentByCardState.Started(paymentOptions, email, paymentId)

        val data3ds = check3DsVersionMethods
            .callCheck3DsVersion(
                paymentId,
                card,
                paymentOptions,
                email
            )

        val finish = finishAuthorizeMethods.finish(
            paymentId,
            card,
            paymentOptions,
            email,
            data3ds.additionalData,
            data3ds.threeDsVersion,
            data3ds.threeDsTransaction
        )
        _state.value = when (finish) {
            is FinishAuthorizeMethods.Result.Need3ds -> PaymentByCardState.ThreeDsUiNeeded(
                finish.threeDsState,
                paymentOptions
            )

            is FinishAuthorizeMethods.Result.Success -> PaymentByCardState.Success(
                finish.paymentId,
                card.cardId,
                rebillId
            )

            is FinishAuthorizeMethods.Result.NeedGetState -> {
                startGetStatePolling(finish.paymentId, paymentId, paymentOptions)
                return
            }
        }
    }

    private suspend fun handleException(
        paymentOptions: PaymentOptions,
        throwable: Throwable,
        paymentId: Long?,
        needCheckRejected: Boolean
    ) {
        if (throwable is CancellationException) return
        withContext(NonCancellable) {
            _state.emit(
                if (needCheckRejected && checkRejectError(throwable)) {
                    PaymentByCardState.CvcUiNeeded(paymentOptions, saveRejectedId())
                } else {
                    PaymentByCardState.Error(throwable, paymentId)
                }
            )
        }
    }

    private fun checkRejectError(it: Throwable): Boolean {
        return it is AcquiringApiException && it.response!!.errorCode == AcquiringApi.API_ERROR_CODE_CHARGE_REJECTED
    }

    private fun saveRejectedId(): String {
        val value = checkNotNull(_paymentIdOrNull?.toString())
        rejectedPaymentId.value = value
        return value
    }

    companion object {

        private var value: RecurrentPaymentProcess? = null

        @JvmStatic
        fun get() = value!!

        @JvmStatic
        @Synchronized
        fun init(
            sdk: AcquiringSdk,
            application: Application,
            threeDsDataCollector: ThreeDsDataCollector = ThreeDsHelper.CollectData
        ) {
            value = RecurrentPaymentProcess(
                InitMethodsSdkImpl(sdk),
                ChargeMethodsSdkImpl(sdk),
                Check3DsVersionMethodsSdkImpl(sdk, application, threeDsDataCollector),
                FinishAuthorizeMethodsSdkImpl(sdk),
                CoroutineScope(Job()),
                CoroutineManager(),
                GetStatusPooling(sdk),
            )
        }
    }
}
