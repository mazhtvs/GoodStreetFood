package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import dataproviders.card.Card
import mocks.models.CardResponse

object GetCardListMock : BaseMock() {

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/v2/GetCardList"))

    private var cardsList = mutableListOf(
        CardResponse(
            pan = "543211******4773",
            cardId = "4750",
            status = "A",
            rebillId = "145919",
        ),
        CardResponse(
            pan = "411111******1111",
            cardId = "5100",
            status = "A",
            rebillId = "145917"
        )
    )

    private var cardsListNoActive = mutableListOf(
        CardResponse(
            pan = "543211******4773",
            cardId = "4750",
            status = "D",
            rebillId = "145919",
        )
    )

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(cardsList)

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }

    fun successNoActiveCard(withDelay: Int = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(cardsListNoActive)

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }

    fun withCard(card: Card, withDelay: Int = 0): ResponseDefinitionBuilder {
        val cardResponse = CardResponse(
            pan = "${card.bin}******${card.pan}",
            cardId = card.cardId,
            status = card.status,
            rebillId = card.rebillId
        )

        cardsList = mutableListOf(cardResponse)

        val jsonBody = Gson().toJson(cardsList)

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }

    fun withCards(vararg card: Card, withDelay: Int = 0): ResponseDefinitionBuilder {
        cardsList = mutableListOf()

        card.forEach {
            val cardResponse = CardResponse(
                pan = "${it.bin}******${it.pan}",
                cardId = it.cardId,
                status = it.status,
                rebillId = it.rebillId
            )
            cardsList.add(cardResponse)
        }


        val jsonBody = Gson().toJson(cardsList)

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }

    fun addCardToList(addedCard: Card): ResponseDefinitionBuilder {
        val cardResponse = CardResponse(
            pan = "${addedCard.bin}******${addedCard.pan}",
            cardId = addedCard.cardId,
            status = addedCard.status,
            rebillId = addedCard.rebillId
        )

        cardsList.add(cardResponse)
        return success()
    }
}


