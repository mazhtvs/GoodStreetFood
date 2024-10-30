package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import mocks.models.AddCardResponse

object AddCardMock : BaseMock() {

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/v2/AddCard"))

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        val addCardResponse = AddCardResponse(
                customerKey = "TestSDK_CustomerKey1123413431",
                requestKey = "1c5838e3-0e8f-4c94-8b4b-fe33771f3fca",
                paymentId = null,
                success = true,
                errorCode = "0"
        )

        val jsonBody = Gson().toJson(
                addCardResponse
        )

        return respondWith(
                jsonBody = jsonBody,
                delayInMilliseconds = withDelay
        )
    }

    fun threeDs(withDelay: Int = 0): ResponseDefinitionBuilder {
        val addCardResponse = AddCardResponse(
                customerKey = "TestSDK_CustomerKey1123413431",
                requestKey = "1c5838e3-0e8f-4c94-8b4b-fe33771f3fca",
                paymentId = 7517468843,
                success = true,
                errorCode = "0"
        )

        val jsonBody = Gson().toJson(
                addCardResponse
        )

        return respondWith(
                jsonBody = jsonBody,
                delayInMilliseconds = withDelay
        )
    }
}