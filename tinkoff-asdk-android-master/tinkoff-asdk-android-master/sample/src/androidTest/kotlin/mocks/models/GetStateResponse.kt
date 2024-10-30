package mocks.models

import com.google.gson.annotations.SerializedName

data class GetStateResponse(
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

        @SerializedName("Params")
        var params: List<ParamsGetState?>? = null
)

data class ParamsGetState(
        @SerializedName("Key")
        var key: String? = null,

        @SerializedName("Value")
        var value: String? = null
)