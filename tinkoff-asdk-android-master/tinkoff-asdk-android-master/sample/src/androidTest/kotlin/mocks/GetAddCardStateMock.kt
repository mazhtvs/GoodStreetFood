package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import dataproviders.card.Card
import mocks.models.GetAddCardStateResponse
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus

object GetAddCardStateMock : BaseMock() {

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/v2/GetAddCardState"))

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
            GetAddCardStateResponse(
                status = ResponseStatus.COMPLETED,
                requestKey = "1c5838e3-0e8f-4c94-8b4b-fe33771f3fca"
            )
        )

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }

    fun threeDs(card: Card, withDelay: Int = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
            GetAddCardStateResponse(
                success = true,
                errorCode = "0",
                status = ResponseStatus.COMPLETED,
                requestKey = "1c5838e3-0e8f-4c94-8b4b-fe33771f3fca",
                cardId = card.cardId
            )
        )

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }
}