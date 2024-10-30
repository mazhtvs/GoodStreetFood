package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import dataproviders.card.Card
import mocks.models.AttachCardResponse
import tests.BASE_URL

object AttachCardMock : BaseMock() {

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/v2/AttachCard"))

    fun success(card: Card, withDelay: Int = 0): ResponseDefinitionBuilder {
        val addCardResponse = AttachCardResponse(
                customerKey = "TestSDK_CustomerKey1123413431",
                requestKey = "1c5838e3-0e8f-4c94-8b4b-fe33771f3fca",
                paymentId = null,
                success = true,
                errorCode = "0",
                cardId = card.cardId
        )

        val jsonBody = Gson().toJson(
                addCardResponse
        )

        return respondWith(
                jsonBody = jsonBody,
                delayInMilliseconds = withDelay
        )
    }

    fun threeDs(card: Card, withDelay: Int = 0): ResponseDefinitionBuilder {
        val addCardResponse = AttachCardResponse(
                customerKey = "TestSDK_CustomerKey1123413431",
                requestKey = "1c5838e3-0e8f-4c94-8b4b-fe33771f3fca",
                paymentId = null,
                success = true,
                errorCode = "0",
                cardId = card.cardId,
                status = "3DS_CHECKING",
                tdsServerTransId = "0cb3c1a1-18e0-4dab-8e99-70daa646c85a",
                acsTransId = "d6ca3d1b-43ab-4a4f-a7de-6d1285dace57",
                acsUrl = "${BASE_URL}/challenge"
        )

        val jsonBody = Gson().toJson(
                addCardResponse
        )

        return respondWith(
                jsonBody = jsonBody,
                delayInMilliseconds = withDelay
        )
    }

    fun cardNotSupport3DS() {
        val addCardResponse = AttachCardResponse(
                success = false,
                errorCode = "106",
                message = "Карта не поддерживает 3DS проверку. Попробуйте другую карту.",
                terminalKey = "TestSDK",
                requestKey = "d85e8a0b-67a9-40ba-9cf5-8083d7444b89",
                cardId = "601188592"
        )
    }
}