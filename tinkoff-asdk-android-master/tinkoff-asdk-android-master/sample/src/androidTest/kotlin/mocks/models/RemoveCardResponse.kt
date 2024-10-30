package mocks.models

import com.google.gson.annotations.SerializedName

data class RemoveCardResponse(
    @SerializedName("Success")
    val success: Boolean? = null,
    @SerializedName("ErrorCode")
    val errorCode: String? = null,
    @SerializedName("TerminalKey")
    val terminalKey: String? = null,
    @SerializedName("CustomerKey")
    val customerKey: String? = null,
    @SerializedName("CardId")
    val cardId: String? = null,
    @SerializedName("Status")
    val status: String? = null,
    @SerializedName("CardType")
    val cardType: Int? = null
)