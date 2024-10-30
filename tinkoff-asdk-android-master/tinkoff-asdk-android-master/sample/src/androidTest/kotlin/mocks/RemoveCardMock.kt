package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson

import mocks.models.RemoveCardResponse

object RemoveCardMock : BaseMock() {
    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/v2/RemoveCard"))

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
            RemoveCardResponse(
                success = true,
                errorCode = "0",
                terminalKey = "TestSDK_CustomerKey1123413431",
                status = "D",
                cardId = "611924916",
                cardType = 0
            )
        )

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }
}