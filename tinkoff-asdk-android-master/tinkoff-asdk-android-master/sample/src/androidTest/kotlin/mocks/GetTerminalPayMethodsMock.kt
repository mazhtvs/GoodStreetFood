@file:Suppress("UNUSED_EXPRESSION")

package mocks

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import mocks.models.GetTerminalPayMethodResponse
import mocks.models.Params
import mocks.models.PayMethod
import mocks.models.Paymethods
import mocks.models.PayMethod.*
import mocks.models.TerminalInfo

object GetTerminalPayMethodsMock : BaseMock() {
    override val matcher: MappingBuilder
        get() = WireMock.get(WireMock.urlEqualTo("/v2/GetTerminalPayMethods?TerminalKey=TestSDK&PaySource=SDK"))

    private val yandexPay = Paymethods(
        payMethod = YANDEX_PAY,
        params = Params(
            merchantId = "6768",
            merchantName = "Horns and hooves",
            merchantOrigin = "https://horns-and-hooves.ru",
            showcaseId = "ShowcaseId"
        )
    )

    private val tinkoffPay = Paymethods(
        payMethod = TINKOFF_PAY,
        params = Params(
            version = "2.0",
            sendPhone = false
        )
    )

    private val mirPay = Paymethods(
        payMethod = MIR_PAY,
    )

    private val sbp = Paymethods(
        payMethod = SBP,
        params = Params(
            merchantLegalId = 6768
        )
    )

    fun success(withDelay: Int = 0): ResponseDefinitionBuilder {
        val jsonBody = Gson().toJson(
            GetTerminalPayMethodResponse(
                success = true,
                errorCode = "0",
                terminalInfo = TerminalInfo(
                    addCardScheme = true,
                    tokenRequired = false,
                    initTokenRequired = false,
                    paymethods = listOf(
                        Paymethods(
                            payMethod = SBP,
                            params = Params(
                                merchantLegalId = 6768
                            )
                        )
                    )
                )
            )
        )

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = withDelay
        )
    }

    fun withPayMethods(
        addCardScheme: Boolean,
        vararg methods: PayMethod
    ): ResponseDefinitionBuilder {
        val payMethods = mutableListOf<Paymethods>()

        methods.forEach {
            when (it) {
                YANDEX_PAY -> payMethods.add(yandexPay)
                TINKOFF_PAY -> payMethods.add(tinkoffPay)
                MIR_PAY -> payMethods.add(mirPay)
                SBP -> payMethods.add(sbp)
                else -> {}
            }
        }

        val jsonBody = Gson().toJson(
            GetTerminalPayMethodResponse(
                success = true,
                errorCode = "0",
                terminalInfo = TerminalInfo(
                    addCardScheme = addCardScheme,
                    tokenRequired = false,
                    initTokenRequired = false,
                    paymethods = payMethods
                )
            )
        )

        return respondWith(
            jsonBody = jsonBody,
            delayInMilliseconds = 0
        )
    }
}