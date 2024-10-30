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
import ru.tinkoff.acquiring.sample.example.utils.toast
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.mirpay.MirPayLauncher
import ru.tinkoff.acquiring.sdk.responses.Paymethod
import ru.tinkoff.acquiring.sdk.utils.Money
import ru.tinkoff.acquiring.sdk.utils.isPackageInstalled

/**
 * Пример запуска процесса оплаты Мир Pay по нажатию на кнопку
 *
 * @author s.y.biryukov
 */
class MirPayPaymentExampleActivity : AppCompatActivity() {

    /**
     * Лончер для запуска процесса оплаты
     */
    private val launcher =
        registerForActivityResult(MirPayLauncher.Contract, ::handlePaymentResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.example_payment_mirpay)

        AcquiringSdk.isDebug = true // Включение логгирования

        val terminal = getTerminalInfo()

        val tinkoffAcquiring = TinkoffAcquiring(
            context = this,
            terminalKey = terminal.terminalKey,
            publicKey = terminal.terminalPublicKey
        )

        tinkoffAcquiring.checkTerminalInfo( // Получение информации о доступных способах оплаты
            onSuccess = { terminalInfo ->
                val mirPayButton = findViewById<View>(R.id.mir_pay_button)
                val isMirPayEnabled = terminalInfo?.paymethods?.any { it.paymethod == Paymethod.MirPay } == true
                if (isMirPayEnabled) {  // Проверяем что данный способ оплаты доступен
                    mirPayButton.isVisible = true
                    mirPayButton.setOnClickListener { startPayment(terminal) }
                }
            },
            onFailure = {
                // Ошибка при получении информации о способах оплаты
                Log.e(TAG, it.message, it)
            }
        )
    }

    private fun startPayment(terminal: TerminalData) {
        val isMirPayInstalled = packageManager.isPackageInstalled(MIR_PAY_PACKAGE_ID)
        if (isMirPayInstalled) { // Проверяем что приложение Mir Pay установлено
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

            val startData = MirPayLauncher.StartData(
                paymentOptions = options
            )

            launcher.launch(startData)
        } else {
            toast("Приложение Mir Pay не установлено")
        }
    }

    /**
     * Обработка результата
     */
    private fun handlePaymentResult(result: MirPayLauncher.Result) {
        when (result) {
            MirPayLauncher.Canceled -> {
                Toast.makeText(this, "Отменено", Toast.LENGTH_LONG).show()
            }

            is MirPayLauncher.Error -> {
                Toast.makeText(
                    this,
                    "Ошибка: ${result.error.message} Код: ${result.errorCode}",
                    Toast.LENGTH_LONG
                ).show()
            }

            is MirPayLauncher.Success -> {
                Toast.makeText(
                    this,
                    "Платеж выполнен успешно. paymentId = ${result.paymentId}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "SAMPLE"
        private const val MIR_PAY_PACKAGE_ID = "ru.nspk.mirpay"
    }
}
