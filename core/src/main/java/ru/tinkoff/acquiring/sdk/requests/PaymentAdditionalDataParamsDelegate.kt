package ru.tinkoff.acquiring.sdk.requests

import ru.tinkoff.acquiring.sdk.requests.AcquiringRequest.Companion.CONNECTION_TYPE
import ru.tinkoff.acquiring.sdk.requests.AcquiringRequest.Companion.DATA
import ru.tinkoff.acquiring.sdk.requests.AcquiringRequest.Companion.DEVICE_MODEL
import ru.tinkoff.acquiring.sdk.requests.AcquiringRequest.Companion.DEVICE_OS
import ru.tinkoff.acquiring.sdk.requests.AcquiringRequest.Companion.SDK_VERSION
import ru.tinkoff.acquiring.sdk.requests.AcquiringRequest.Companion.SOFTWARE_VERSION
import kotlin.collections.HashMap

/**
 * @author k.shpakovskiy
 */
interface PaymentAdditionalDataParamsDelegate {
    var sdkVersion: String?
    var softwareVersion: String?
    var deviceModel: String?

    fun MutableMap<String, Any>.writeDataParams(
        data: Map<String, String>?,
        additionalParams: (HashMap<String, String>.() -> Unit)? = null
    )
}

class PaymentAdditionalDataParamsImpl : PaymentAdditionalDataParamsDelegate {
    override var sdkVersion: String? = null
    override var softwareVersion: String? = null
    override var deviceModel: String? = null

    override fun MutableMap<String, Any>.writeDataParams(
        data: Map<String, String>?,
        additionalParams: (HashMap<String, String>.() -> Unit)?
    ) {
        val resultData = HashMap<String, String>()

        if (additionalParams != null){ resultData.apply(additionalParams) }

        if (data != null) { resultData.putAll(data) }

        resultData[DEVICE_OS] = DEVICE_OS_ANDROID
        resultData[CONNECTION_TYPE] = CONNECTION_TYPE_MOBILE_SDK
        sdkVersion?.let { resultData[SDK_VERSION] = it }
        softwareVersion?.let { resultData[SOFTWARE_VERSION] = it }
        deviceModel?.let { resultData[DEVICE_MODEL] = it }

        this[DATA] = resultData
    }

    private companion object {
        private const val CONNECTION_TYPE_MOBILE_SDK = "mobile_sdk"
        private const val DEVICE_OS_ANDROID = "Android"
    }
}
