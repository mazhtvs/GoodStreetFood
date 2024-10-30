package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.gson.Gson
import mocks.models.TinkoffPayParams
import mocks.models.TinkoffPayResponse

object TinkoffPayLinkMock : BaseMock() {
    override val matcher: MappingBuilder
        get() = get(urlPathEqualTo("/v2/TinkoffPay/transactions/5677499183/versions/2.0/link"))

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
            TinkoffPayResponse(
                success = true,
                errorCode = "0",
                params = TinkoffPayParams(
                    redirectUrl = "https://www.tinkoff.ru/tpay/2000000000000037338",
                    webQR = "https://securepay.tinkoff.ru/v2/TinkoffPay/2332790853/QR"
                )
            )
        )

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }
}