package ru.tinkoff.acquiring.sample.example.utils

import ru.tinkoff.acquiring.sample.SampleApplication
import ru.tinkoff.acquiring.sample.utils.TerminalsManager
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.utils.Money

/**
 * Created by v.budnitskiy
 */

fun createSavedCardOptions(isRecurrent: Boolean): SavedCardsOptions {
    // Выбранный терминал
    val params = TerminalsManager.selectedTerminal

    return SampleApplication.tinkoffAcquiring.savedCardsOptions {
        mode = SavedCardsOptions.Mode.PAYMENT
        withArrowBack = true
        showOnlyRecurrentCards = isRecurrent
        allowNewCard = !isRecurrent
        customerOptions {
            customerKey = params.customerKey
            email = params.customerEmail
            checkType = "NO"
        }
    }
}

fun createPaymentOptions(isParentRecurrent: Boolean = false): PaymentOptions {
    val sessionParams = TerminalsManager.selectedTerminal

    return PaymentOptions()
        .setOptions {
            orderOptions {
                orderId = "orderId"
                amount = Money.ofRubles(100)
                title = "title"
                recurrentPayment = isParentRecurrent
            }
            setTerminalParams(
                sessionParams.terminalKey,
                sessionParams.publicKey
            )
        }
}