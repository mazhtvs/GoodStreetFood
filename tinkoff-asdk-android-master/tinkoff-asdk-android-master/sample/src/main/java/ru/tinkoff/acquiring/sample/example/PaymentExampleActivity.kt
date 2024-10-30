package ru.tinkoff.acquiring.sample.example

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.example.utils.getTerminalInfo
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.mainform.MainFormLauncher
import ru.tinkoff.acquiring.sdk.utils.Money

/**
 * Пример запуска процесса оплаты через платежную форму
 *
 * @author s.y.biryukov
 */
class PaymentExampleActivity : AppCompatActivity() {

    /**
     * Лончер для запуска платежной формы
     */
    private val launcher =
        registerForActivityResult(MainFormLauncher.Contract, ::handlePaymentResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.example_payment_card)

        AcquiringSdk.isDebug = true // Включение логгирования

        val button = R.id.btn_buy_by_card
        findViewById<TextView>(button).setOnClickListener { startPayment() }
    }

    private fun startPayment() {
        val terminal = getTerminalInfo() // Получение данных о терминале

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

        val startData = MainFormLauncher.StartData(
            paymentOptions = options,
        )
        //</editor-fold>

        launcher.launch(startData) // Запуск процесса оплаты
    }

    /**
     * Обработка результата
     */
    private fun handlePaymentResult(result: MainFormLauncher.Result) {
        when (result) {
            MainFormLauncher.Canceled -> {
                Toast.makeText(this, "Отменено", Toast.LENGTH_LONG).show()
            }

            is MainFormLauncher.Error -> {
                Toast.makeText(
                    this,
                    "Ошибка: ${result.error.message} Код: ${result.errorCode}",
                    Toast.LENGTH_LONG
                ).show()
            }

            is MainFormLauncher.Success -> {
                Toast.makeText(
                    this,
                    "Платеж выполнен успешно. paymentId = ${result.paymentId}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
