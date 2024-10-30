package mocks.models

import com.google.gson.annotations.SerializedName
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus
import java.io.Serializable

data class GetAddCardStateResponse(
        @SerializedName("Success")
        var success: Boolean? = null,

        @SerializedName("ErrorCode")
        var errorCode: String? = null,

        @SerializedName("RequestKey")
        val requestKey: String? = null,

        @SerializedName("Status")
        val status: ResponseStatus? = null,

        @SerializedName("CardId")
        val cardId: String? = null,

        @SerializedName("RebillId")
        val rebillId: String? = null
) : Serializable