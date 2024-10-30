package mocks.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CardResponse(
        @SerializedName("Pan")
        var pan: String? = null,

        @SerializedName("CardId")
        var cardId: String? = null,

        @SerializedName("ExpDate")
        var expDate: String? = null,

        @SerializedName("Status")
        var status: String? = null,

        @SerializedName("RebillId")
        var rebillId: String? = null

) : Serializable