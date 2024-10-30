package ru.tinkoff.acquiring.sample.example

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.example.utils.getTerminalInfo
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.payment.PaymentByCardLauncher
import ru.tinkoff.acquiring.sdk.utils.Money

/**
 * Пример запуска процесса оплаты новой картой по нажатию на кнопку
 *
 * @author s.y.biryukov
 */
class CardPaymentExampleActivity : AppCompatActivity() {

    /**
     * Лончер для запуска процесса оплаты
     */
    private val launcher =
        registerForActivityResult(PaymentByCardLauncher.Contract, ::handlePaymentResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.example_payment_card)

        AcquiringSdk.isDebug = true // Включение логгирования

        findViewById<TextView>(R.id.btn_buy_by_card).setOnClickListener { startPayment() }
    }

    private fun startPayment() {
        val terminal = getTerminalInfo()

        //<editor-fold desc="Формирование параметров платежа">
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

        val startData = PaymentByCardLauncher.StartData(
            paymentOptions = options,
            cards = emptyList()
        )
        //</editor-fold>

        launcher.launch(startData) // Запуск процесса оплаты
    }

    /**
     * Обработка результата
     */
    private fun handlePaymentResult(result: PaymentByCardLauncher.Result) {
        when (result) {
            PaymentByCardLauncher.Canceled -> {
                Toast.makeText(this, "Отменено", Toast.LENGTH_LONG).show()
            }

            is PaymentByCardLauncher.Error -> {
                Toast.makeText(
                    this,
                    "Ошибка: ${result.error.message} Код: ${result.errorCode}",
                    Toast.LENGTH_LONG
                ).show()
            }

            is PaymentByCardLauncher.Success -> {
                Toast.makeText(
                    this,
                    "Платеж выполнен успешно. paymentId = ${result.paymentId}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
