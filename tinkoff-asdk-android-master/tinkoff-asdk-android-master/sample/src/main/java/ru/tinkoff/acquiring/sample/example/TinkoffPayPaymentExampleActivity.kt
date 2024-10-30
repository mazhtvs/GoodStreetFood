package ru.tinkoff.acquiring.sample.example

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.example.utils.TerminalData
import ru.tinkoff.acquiring.sample.example.utils.getTerminalInfo
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.tpay.TpayLauncher
import ru.tinkoff.acquiring.sdk.redesign.tpay.models.getTinkoffPayVersion
import ru.tinkoff.acquiring.sdk.responses.Paymethod
import ru.tinkoff.acquiring.sdk.utils.Money

/**
 * Пример запуска процесса оплаты Tinkoff Pay по нажатию на кнопку
 *
 * @author s.y.biryukov
 */
class TinkoffPayPaymentExampleActivity: AppCompatActivity() {

    /**
     * Лончер для запуска процесса оплаты
     */
    private val launcher =
        registerForActivityResult(TpayLauncher.Contract, ::handlePaymentResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.example_payment_tpay)

        val terminal = getTerminalInfo() // Получение данных о терминале

        AcquiringSdk.isDebug = true // Включение логгирования

        val tinkoffAcquiring = TinkoffAcquiring(
            context = this,
            terminalKey = terminal.terminalKey,
            publicKey = terminal.terminalPublicKey
        )

        tinkoffAcquiring.checkTerminalInfo( // Получение информации о доступных способах оплаты
            onSuccess = { terminalInfo ->
                val isTinkoffPayEnabled = terminalInfo?.paymethods?.any { it.paymethod == Paymethod.TinkoffPay } == true
                val version = terminalInfo?.getTinkoffPayVersion()
                if (isTinkoffPayEnabled && version != null) { // Проверяем что Tinkoff Pay доступно
                    val button = findViewById<View>(R.id.tinkoff_pay_button)
                    button.isVisible = true
                    button.setOnClickListener { startPayment(terminal, version) }
                }
            },
            onFailure = {
                Log.e(TAG, it.message, it)
            }
        )
    }

    private fun startPayment(
        terminal: TerminalData,
        version: String
    ) {
        val options = PaymentOptions().setOptions {
            setTerminalParams(
                terminalKey = terminal.terminalKey,
                publicKey = terminal.terminalPublicKey
            )
            orderOptions {
                orderId = "order_id" // Идентификатор заказа
                amount = Money.ofRubles(10) // Сумма заказа
            }
        }
        val startData = TpayLauncher.StartData(
            paymentOptions = options,
            version = version
        )

        launcher.launch(startData)
    }

    private fun handlePaymentResult(result: TpayLauncher.Result?) {
        when (result) {
            TpayLauncher.Canceled -> {
                Toast.makeText(this, "Отменено", Toast.LENGTH_LONG).show()
            }

            is TpayLauncher.Error -> {
                Toast.makeText(
                    this,
                    "Ошибка: ${result.error.message} Код: ${result.errorCode}",
                    Toast.LENGTH_LONG
                ).show()
            }

            is TpayLauncher.Success -> {
                Toast.makeText(
                    this,
                    "Платеж выполнен успешно. paymentId = ${result.paymentId}",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> Unit
        }
    }

    companion object {
        const val TAG = "SAMPLE"
    }
}
