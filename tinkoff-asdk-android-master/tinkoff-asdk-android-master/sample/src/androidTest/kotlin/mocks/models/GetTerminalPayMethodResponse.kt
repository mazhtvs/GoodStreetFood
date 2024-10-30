package mocks.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import ru.tinkoff.acquiring.sample.R

data class GetTerminalPayMethodResponse(
    @SerializedName("Success")
    var success: Boolean? = null,

    @SerializedName("ErrorCode")
    var errorCode: String? = null,

    @SerializedName("TerminalInfo")
    var terminalInfo: TerminalInfo? = null,

    ) : Serializable

data class TerminalInfo(
    @SerializedName("AddCardScheme")
    var addCardScheme: Boolean? = null,

    @SerializedName("TokenRequired")
    var tokenRequired: Boolean? = null,

    @SerializedName("InitTokenRequired")
    var initTokenRequired: Boolean? = null,

    @SerializedName("Paymethods")
    var paymethods: List<Paymethods?>? = null
) : Serializable

data class Paymethods(
    @SerializedName("PayMethod")
    var payMethod: PayMethod? = null,

    @SerializedName("Params")
    var params: Params? = null
) : Serializable

enum class PayMethod(
    val primaryText: String?,
    val primaryIcon: Int?,
    val secondaryTitle: String?,
    val secondarySubtitle: String?
) {
    @SerializedName("YandexPay")
    YANDEX_PAY(null, null, null, null),

    @SerializedName("TinkoffPay")
    TINKOFF_PAY("Оплатить с Тинькофф", R.drawable.acq_icon_tinkoff_pay, "Tinkoff Pay", "В приложении банка"),

    @SerializedName("MirPay")
    MIR_PAY("Оплатить с", R.drawable.acq_ic_wallet_mir_pay, "Mir Pay", "В приложении Mir Pay"),

    @SerializedName("SBP")
    SBP("Оплатить с", R.drawable.acq_ic_sbp_primary_button_logo, "CБП", "В приложении любого банка"),

    @SerializedName("CARD")
    CARD("Оплатить картой", null, "Картой", null)
}

data class Params(
    @SerializedName("MerchantName")
    var merchantName: String? = null,

    @SerializedName("ShowcaseId")
    var showcaseId: String? = null,

    @SerializedName("MerchantId")
    var merchantId: String? = null,

    @SerializedName("MerchantOrigin")
    var merchantOrigin: String? = null,

    @SerializedName("Person")
    var person: Boolean? = null,

    @SerializedName("Version")
    var version: String? = null,

    @SerializedName("SendPhone")
    var sendPhone: Boolean? = null,

    @SerializedName("MerchantLegalId")
    var merchantLegalId: Int? = null
) : Serializable
