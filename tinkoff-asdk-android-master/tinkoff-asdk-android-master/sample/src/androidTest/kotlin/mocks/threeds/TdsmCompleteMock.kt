package mocks.threeds

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import mocks.BaseMock
import tests.BASE_URL

object TdsmCompleteMock : BaseMock() {

    private const val html = """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
            <html lang="en"
                  xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <meta charset="UTF-8">
                <title>TDSM complete form</title>
            </head>
            <body>
            <form name="tdsmForm" method="post" action="$BASE_URL/v2/Complete3DSMethodv2">
                <input type="hidden" name="threeDSMethodData"
                value="ewogICJ0aHJlZURTU2VydmVyVHJhbnNJRCIgOiAiZDVlYTZhYWItOWJlNC00N2U1LWJlYzUtOThhMWQ1MjY5YzhjIgp9">
            </form>
            <script type="text/javascript">
                document.forms['tdsmForm'].submit();
            </script>
            </body>
            </html>
        """

    override val matcher: MappingBuilder
        get() = WireMock.post(WireMock.urlPathEqualTo("/tdsm/complete"))

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        return respondWithHtml(
            stringBody = html,
            delayInMilliseconds = withDelay
        )
    }
}