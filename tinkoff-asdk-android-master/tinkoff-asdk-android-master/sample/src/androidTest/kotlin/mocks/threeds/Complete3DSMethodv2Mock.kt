package mocks.threeds

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import mocks.BaseMock

object Complete3DSMethodv2Mock : BaseMock() {

    private const val html = """
        <html>
        <body><
        h1>Whitelabel Error Page</h1>
        <p>This application has no explicit mapping for /error, so you are seeing this as a fallback.</p>
        <div id='created'>Mon Jan 22 16:25:54 MSK 2024</div><div>There was an unexpected error (type=Not Found, status=404).
        </div><div>Not Found</div>
        </body>
        </html>
        """

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/v2/Complete3DSMethodv2"))

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        return respondWithHtml(
            stringBody = html,
            delayInMilliseconds = withDelay
        )
    }
}