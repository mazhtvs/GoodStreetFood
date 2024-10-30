package mocks.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Submit3DSAuthorizationV2Response(
        @SerializedName("Success")
        var success: Boolean? = null,

        @SerializedName("ErrorCode")
        var errorCode: String? = null,

        @SerializedName("TerminalKey")
        var terminalKey: String? = null,

        @SerializedName("Status")
        var status: String? = null,

        @SerializedName("PaymentId")
        var paymentId: String? = null,

        @SerializedName("OrderId")
        var orderId: String? = null,

        @SerializedName("Amount")
        var amount: Long? = null
) : Serializable