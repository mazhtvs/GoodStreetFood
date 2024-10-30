package mocks.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AddCardResponse(
        @SerializedName("CustomerKey")
        val customerKey: String? = null,

        @SerializedName("RequestKey")
        val requestKey: String? = null,

        @SerializedName("PaymentId")
        val paymentId: Long? = null,

        @SerializedName("Success")
        val success: Boolean? = null,

        @SerializedName("ErrorCode")
        val errorCode: String? = null
) : Serializable