package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import mocks.models.Check3dsVersionResponse
import tests.BASE_URL

object Check3dsVersionMock : BaseMock() {

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/v2/Check3dsVersion"))

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
            Check3dsVersionResponse(
                success = true,
                errorCode = "0",
                message = "OK",
                version = "2.1.0",
                tdsServerTransID = "0cb3c1a1-18e0-4dab-8e99-70daa646c85a",
                paymentSystem ="mock",
                threeDSMethodURL = "${BASE_URL}/tdsm"
            )
        )

        return Check3dsVersionMock.respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }

    fun non3ds(withDelay: Int = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
            Check3dsVersionResponse(
                success = true,
                errorCode = "0",
                message = "OK",
                version = "2.1.0",
            )
        )

        return Check3dsVersionMock.respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }
}