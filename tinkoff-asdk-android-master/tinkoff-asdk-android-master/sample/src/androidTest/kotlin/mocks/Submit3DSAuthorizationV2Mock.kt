package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import mocks.models.Submit3DSAuthorizationV2Response

object Submit3DSAuthorizationV2Mock : BaseMock() {

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/v2/Submit3DSAuthorizationV2"))

    fun success(withDelay: Int = 0, paymentId: String = "5677499183", orderId: String = "G-+vetKoVodkL1R9ziQP", amount: Long = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
                Submit3DSAuthorizationV2Response(
                        success = true,
                        errorCode = "0",
                        terminalKey = "TestSDK",
                        status = "AUTHORIZED",
                        paymentId = paymentId,
                        orderId = orderId,
                        amount = amount
                )
        )

        return Submit3DSAuthorizationV2Mock.respondWith(
                jsonBody = jsonBody,
                delayInMilliseconds = withDelay
        )
    }
}