package mocks.models

import com.google.gson.annotations.SerializedName

data class TinkoffPayResponse(
    @SerializedName("Success")
    var success: Boolean? = null,

    @SerializedName("ErrorCode")
    var errorCode: String? = null,

    @SerializedName("Params")
    var params: TinkoffPayParams? = null
)

data class TinkoffPayParams(
    @SerializedName("RedirectUrl")
    var redirectUrl: String? = null,

    @SerializedName("WebQR")
    var webQR: String? = null
)