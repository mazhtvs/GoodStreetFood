package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import mocks.models.FinishAuthorizeResponse
import tests.BASE_URL

object FinishAuthorizeMock : BaseMock() {
    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/v2/FinishAuthorize"))

    fun success(withDelay: Int = 0, paymentId: String = "5677499183", orderId: String = "G-+vetKoVodkL1R9ziQP", amount: Long): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
                FinishAuthorizeResponse(
                        success = true,
                        errorCode = "0",
                        terminalKey = "TestSDK_CustomerKey1123413431",
                        status = "3DS_CHECKING",
                        paymentId = paymentId,
                        orderId = orderId,
                        amount = amount,
                        aCSUrl = "$BASE_URL/challenge",
                        tdsServerTransId = "d94972a7-94d2-4eb7-bb18-8a2be30394f5",
                        acsTransId = "c24d15de-806e-45f1-a074-a1f046a2b809"
                )
        )

        return respondWith(
                jsonBody = jsonBody,
                delayInMilliseconds = withDelay
        )
    }

    fun non3ds(withDelay: Int = 0, paymentId: String = "5677499183", orderId: String = "G-+vetKoVodkL1R9ziQP", amount: Long): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
                FinishAuthorizeResponse(
                        success = true,
                        errorCode = "0",
                        terminalKey = "TestSDK_CustomerKey1123413431",
                        status = "CONFIRMED",
                        paymentId = paymentId,
                        orderId = orderId,
                        amount = amount
                )
        )

        return respondWith(
                jsonBody = jsonBody,
                delayInMilliseconds = withDelay
        )
    }
}