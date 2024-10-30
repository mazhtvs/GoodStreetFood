package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.google.gson.Gson
import mocks.models.InitResponse

object InitMock : BaseMock() {

    private val urlPattern = WireMock.urlPathEqualTo("/v2/Init")

    override val matcher: MappingBuilder
        get() = WireMock.post(urlPattern)

    val requestPattern: RequestPatternBuilder = WireMock.postRequestedFor(urlPattern)

    fun success(
        withDelay: Int = 0,
        paymentId: String = "5677499183",
        orderId: String = "G-+vetKoVodkL1R9ziQP",
        amount: Long = 10000
    ): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
            InitResponse(
                success = true,
                errorCode = "0",
                terminalKey = "TestSDK_CustomerKey1123413431",
                status = "NEW",
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