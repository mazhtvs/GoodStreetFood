package mocks.models

import com.google.gson.annotations.SerializedName

data class FinishAuthorizeResponse(
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
        var amount: Long? = null,

        @SerializedName("ACSUrl")
        var aCSUrl: String? = null,

        @SerializedName("TdsServerTransId")
        var tdsServerTransId: String? = null,

        @SerializedName("AcsTransId")
        var acsTransId: String? = null
)