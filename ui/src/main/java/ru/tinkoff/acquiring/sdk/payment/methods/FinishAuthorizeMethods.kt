package ru.tinkoff.acquiring.sdk.payment.methods

import android.os.Build
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.BuildConfig
import ru.tinkoff.acquiring.sdk.models.PaymentSource
import ru.tinkoff.acquiring.sdk.models.ThreeDsState
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.requests.FinishAuthorizeRequest
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsAppBasedTransaction
import ru.tinkoff.acquiring.sdk.utils.checkNotNull
import ru.tinkoff.acquiring.sdk.utils.getIpAddress

/**
 * Created by i.golovachev
 */
interface FinishAuthorizeMethods {

    sealed interface Result {
        class Need3ds(val threeDsState: ThreeDsState, paymentOptions: PaymentOptions) : Result
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
    }

    suspend fun finish(
        paymentId: Long,
        paymentSource: PaymentSource,
        paymentOptions: PaymentOptions,
        email: String? = null,
        data: Map<String, String>? = null,
        threeDsVersion: String? = null,
        threeDsTransaction: ThreeDsAppBasedTransaction? = null
    ): Result
}


internal class FinishAuthorizeMethodsSdkImpl(
    private val acquiringSdk: AcquiringSdk
) : FinishAuthorizeMethods {

    override suspend fun finish(
        paymentId: Long,
        paymentSource: PaymentSource,
        paymentOptions: PaymentOptions,
        email: String?,
        data: Map<String, String>?,
        threeDsVersion: String?,
        threeDsTransaction: ThreeDsAppBasedTransaction?
    ): FinishAuthorizeMethods.Result {
        val ipAddress = if (data != null) getIpAddress() else null
        val finishRequest = acquiringSdk.configureFinishAuthorize(
            paymentId = paymentId,
            ipAddress = ipAddress,
            paymentSource = paymentSource,
            email = email,
            data = data,
        )
        val response = finishRequest.execute()
        val threeDsData = response.getThreeDsData(threeDsVersion)

        return if (threeDsData.isThreeDsNeed) {
            FinishAuthorizeMethods.Result.Need3ds(
                ThreeDsState(threeDsData, threeDsTransaction),
                paymentOptions
            )
        } else {
            val responsePaymentId = response.paymentId.checkNotNull { "paymentId must be not null" }

            when(response.status) {
                in ResponseStatus.processStatuses -> {
                    FinishAuthorizeMethods.Result.NeedGetState(
                        paymentId = responsePaymentId,
                        cardId = null,
                        rebillId = response.rebillId
                    )
                }
                else -> {
                    FinishAuthorizeMethods.Result.Success(
                        paymentId = responsePaymentId,
                        cardId = null,
                        rebillId = response.rebillId
                    )
                }
            }
        }
    }

    private fun AcquiringSdk.configureFinishAuthorize(
        paymentId: Long,
        ipAddress: String?,
        paymentSource: PaymentSource,
        email: String?,
        data: Map<String, String>?,
    ): FinishAuthorizeRequest {
        return finishAuthorize {
            this.paymentId = paymentId
            this.email = email
            this.paymentSource = paymentSource
            this.data = data

            sdkVersion = BuildConfig.ASDK_VERSION_NAME
            softwareVersion = Build.VERSION.SDK_INT.toString()
            deviceModel = Build.MODEL

            ip = ipAddress
            sendEmail = email != null
        }
    }
}
