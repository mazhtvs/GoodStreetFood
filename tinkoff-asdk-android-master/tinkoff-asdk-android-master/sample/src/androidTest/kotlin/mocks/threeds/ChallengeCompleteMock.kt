package mocks.threeds

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import mocks.BaseMock
import tests.BASE_URL

object ChallengeCompleteMock : BaseMock() {

    private const val html = """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
            <html lang="en" xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta charset="UTF-8">
                <title>Test redirect form</title>
            </head>
            <body>
                <form name="redirectForm" method="post" action="${BASE_URL}/v2/Submit3DSAuthorizationV2">
        <input type="hidden"
        name="cres" value="ewogICJ0aHJlZURTU2VydmVyVHJhbnNJRCIgOiAiZDVlYTZhYWItOWJlNC00N2U1LWJlYzUtOThhMWQ1MjY5YzhjIiwKICAiY \
        WNzQ291bnRlckF0b1MiIDogIjAwMSIsCiAgImFjc1RyYW5zSUQiIDogImFjOTNjZDBmLTUyMjYtNDZmZi05M2IyLTJjY2Y2NTFlNWExZCIsCiAgImNoYWxsZW \
        5nZUNvbXBsZXRpb25JbmQiIDogIlkiLAogICJtZXNzYWdlVHlwZSIgOiAiQ1JlcyIsCiAgIm1lc3NhZ2VWZXJzaW9uIiA6ICIyLjEuMCIsCiAgInRyYW5zU3Rh \
        dHVzIiA6ICJZIgp9">
                </form>
                <script type="text/javascript">
                    document.forms['redirectForm'].submit();
                </script>
            </body>
            </html>
        """

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/challenge/complete"))

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        return respondWithHtml(
            stringBody = html,
            delayInMilliseconds = withDelay
        )
    }
}