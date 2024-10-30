package mocks.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Check3dsVersionResponse(
        @SerializedName("Success")
        var success: Boolean? = null,

        @SerializedName("ErrorCode")
        var errorCode: String? = null,

        @SerializedName("Message")
        var message: String? = null,

        @SerializedName("Version")
        var version: String? = null,

        @SerializedName("TdsServerTransID")
        var tdsServerTransID: String? = null,

        @SerializedName("PaymentSystem")
        var paymentSystem: String? = null,

        @SerializedName("ThreeDSMethodURL")
        var threeDSMethodURL: String? = null
) : Serializable
