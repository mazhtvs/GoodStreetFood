package mocks.threeds

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import mocks.BaseMock

object ChallengeMock : BaseMock() {

    private const val html =
        """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta charset="UTF-8">
        <title>Authentication</title>
    </head>
    <body>
        <div style="position: absolute; top: 40vh; right: 40vw; text-align: center;">
            <h1>Please enter your password</h1>
            <form action="/challenge/complete" method="post">
                <input type="text" name="otp">
                <button type="submit" id="sendButton">Send</button>
            </form>
        </div>
    </body>
    </html>
"""

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/challenge"))

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        return respondWithHtml(
            stringBody = html,
            delayInMilliseconds = withDelay
        )
    }
}