package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.google.gson.Gson
import mocks.models.GetStateResponse
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus

object GetStateMock : BaseMock() {

    private val urlPattern = WireMock.urlPathEqualTo("/v2/GetState")
    override val matcher: MappingBuilder
        get() = WireMock.post(urlPattern)

    val requestPattern: RequestPatternBuilder = WireMock.postRequestedFor(urlPattern)

    fun success(
        withDelay: Int = 0,
        paymentId: String = "5677499183",
        orderId: String = "G-+vetKoVodkL1R9ziQP",
        amount: Long = 10000,
        status: ResponseStatus = ResponseStatus.CONFIRMING
    ): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
            GetStateResponse(
                success = true,
                errorCode = "0",
                terminalKey = "TestSDK_CustomerKey1123413431",
                status = status.toString(),
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