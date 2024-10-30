package mocks.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AttachCardResponse(
        @SerializedName("CustomerKey")
        val customerKey: String? = null,

        @SerializedName("RequestKey")
        val requestKey: String? = null,

        @SerializedName("PaymentId")
        val paymentId: Long? = null,

        @SerializedName("Success")
        val success: Boolean? = null,

        @SerializedName("ErrorCode")
        val errorCode: String? = null,

        @SerializedName("CardId")
        val cardId: String? = null,

        @SerializedName("Status")
        val status: String? = null,

        @SerializedName("TdsServerTransId")
        val tdsServerTransId: String? = null,

        @SerializedName("AcsTransId")
        val acsTransId: String? = null,

        @SerializedName("ACSUrl")
        val acsUrl: String? = null,

        @SerializedName("Message")
        val message: String? = null,

        @SerializedName("TerminalKey")
        val terminalKey: String? = null
) : Serializable