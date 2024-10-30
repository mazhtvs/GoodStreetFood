package ru.tinkoff.acquiring.sdk.payment.methods

import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkException
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.network.AcquiringApi
import ru.tinkoff.acquiring.sdk.payment.methods.InitConfigurator.configure
import ru.tinkoff.acquiring.sdk.requests.InitRequest
import ru.tinkoff.acquiring.sdk.responses.InitResponse

/**
 * Created by i.golovachev
 */

interface ChargeMethods {

    sealed interface Result {

        class Success(
            val paymentId: Long,
            val cardId: String?,
            val rebillId: String?
        ) : Result

        class NeedGetState(
            val paymentId: Long,
            val cardId: String?,
            val rebillId: String?
        ) : Result

        class Failure(
            val paymentId: Long,
            val cardId: String?,
            val failure: Throwable
        ) : Result
    }

    suspend fun init(
        paymentOptions: PaymentOptions,
        email: String?,
        rejectedPaymentId: String
    ): InitResponse

    suspend fun charge(paymentId: Long?, rebillId: String?): Result

    suspend fun getCardByRebillId(rebillId: String?, paymentOptions: PaymentOptions): Card
}

internal class ChargeMethodsSdkImpl(private val acquiringSdk: AcquiringSdk) : ChargeMethods {

    override suspend fun init(
        paymentOptions: PaymentOptions,
        email: String?,
        rejectedPaymentId: String
    ): InitResponse {
        return acquiringSdk.configureInit(paymentOptions, email)
            .modifyRejectedData(rejectedPaymentId)
            .execute()
    }

    private fun AcquiringSdk.configureInit(
        paymentOptions: PaymentOptions,
        email: String? = null
    ) = init {
        configure(paymentOptions)
        if (paymentOptions.features.duplicateEmailToReceipt && !email.isNullOrEmpty()) {
            receipt?.email = email
        }
        this.recurrent = true
    }

    private fun InitRequest.modifyRejectedData(rejectedPaymentId: String): InitRequest {
        val data = data?.toMutableMap() ?: mutableMapOf()
        val rejectedDataMap = buildMap {
            put(AcquiringApi.RECURRING_TYPE_KEY, AcquiringApi.RECURRING_TYPE_VALUE)
            put(AcquiringApi.FAIL_MAPI_SESSION_ID, rejectedPaymentId)
        }
        data.putAll(rejectedDataMap)
        this.data = data
        return this
    }

    override suspend fun charge(paymentId: Long?, rebillId: String?): ChargeMethods.Result {
        val response = acquiringSdk.configureCharge(paymentId, rebillId).execute()
        with(response) {
            when (status) {
                in ResponseStatus.processStatuses -> {
                    return ChargeMethods.Result.NeedGetState(
                        paymentId = checkNotNull(paymentId) { "paymentId must be not null" },
                        cardId = null,
                        rebillId = rebillId,
                    )
                }
                in ResponseStatus.unhappyPassStatuses -> {
                    return ChargeMethods.Result.Failure(
                        paymentId = checkNotNull(paymentId) { "paymentId must be not null" },
                        cardId = null,
                        failure = AcquiringSdkException(IllegalStateException("Failure ResponseStatus $status")),
                    )
                }
                else -> {
                    return ChargeMethods.Result.Success(
                        paymentId = checkNotNull(paymentId) { "paymentId must be not null" },
                        cardId = null,
                        rebillId = rebillId,
                    )
                }
            }
        }
    }

    private fun AcquiringSdk.configureCharge(paymentId: Long?, rebillId: String?) = charge {
        this.paymentId = checkNotNull(paymentId) {
            "paymentId must be not null"
        }
        this.rebillId = checkNotNull(rebillId) {
            "rebillId must be not null"
        }
    }

    override suspend fun getCardByRebillId(
        rebillId: String?,
        paymentOptions: PaymentOptions
    ): Card {
        val rebill = checkNotNull(rebillId) { "rebillId must be not null" }
        val list = acquiringSdk.configureGetList(paymentOptions.customer.customerKey).execute()
        return list.cards.first { it.rebillId == rebill }
    }

    private fun AcquiringSdk.configureGetList(customerKey: String?) = getCardList {
        this.customerKey = checkNotNull(customerKey) { "customerKey must be not null" }
    }

}
