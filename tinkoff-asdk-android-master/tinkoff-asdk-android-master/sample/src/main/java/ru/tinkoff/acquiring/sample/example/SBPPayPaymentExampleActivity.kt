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
import ru.tinkoff.acquiring.sdk.redesign.sbp.SbpPayLauncher
import ru.tinkoff.acquiring.sdk.responses.Paymethod
import ru.tinkoff.acquiring.sdk.utils.Money

/**
 * Пример запуска процесса оплаты СБП по нажатию на кнопку
 *
 * @author s.y.biryukov
 */
class SBPPayPaymentExampleActivity : AppCompatActivity() {

    /**
     * Лончер для запуска процесса оплаты
     */
    private val launcher =
        registerForActivityResult(SbpPayLauncher.Contract, ::handlePaymentResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.example_payment_sbppay)

        AcquiringSdk.isDebug = true // Включение логгирования

        val terminal = getTerminalInfo()

        val tinkoffAcquiring = TinkoffAcquiring(
            context = this,
            terminalKey = terminal.terminalKey,
            publicKey = terminal.terminalPublicKey
        )

        tinkoffAcquiring.checkTerminalInfo( // Получение информации о доступных способах оплаты
            onSuccess = { terminalInfo ->
                val sbpButton = findViewById<View>(R.id.btn_fps_pay)
                val isSbpEnabled =
                    terminalInfo?.paymethods?.any { it.paymethod == Paymethod.SBP } == true
                if (isSbpEnabled) {  // Проверяем что данный способ оплаты доступен
                    sbpButton.isVisible = true
                    sbpButton.setOnClickListener { startPayment(tinkoffAcquiring, terminal) }
                }
            },
            onFailure = {
                // Ошибка при получении информации о способах оплаты
                Log.e(TAG, it.message, it)
            }
        )
    }

    private fun startPayment(
        tinkoffAcquiring: TinkoffAcquiring,
        terminal: TerminalData
    ) {
        tinkoffAcquiring.initSbpPaymentSession()
        val options = PaymentOptions().setOptions {
            setTerminalParams(
                terminalKey = terminal.terminalKey,
                publicKey = terminal.terminalPublicKey
            )
            orderOptions {
                orderId = "order_id_sbp" // Идентификатор заказа
                amount = Money.ofRubles(10) // Сумма заказа
            }
        }

        val startData = SbpPayLauncher.StartData(
            paymentOptions = options
        )

        launcher.launch(startData)
    }

    /**
     * Обработка результата
     */
    private fun handlePaymentResult(result: SbpPayLauncher.Result) {
        when (result) {
            is SbpPayLauncher.Canceled -> {
                Toast.makeText(this, "Отменено", Toast.LENGTH_LONG).show()
            }

            is SbpPayLauncher.Error -> {
                Toast.makeText(
                    this,
                    "Ошибка: ${result.error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            is SbpPayLauncher.Success -> {
                Toast.makeText(
                    this,
                    "Платеж выполнен успешно. paymentId = ${result.payment}",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> Unit
        }
    }

    companion object {
        private const val TAG = "SAMPLE"
    }
}
