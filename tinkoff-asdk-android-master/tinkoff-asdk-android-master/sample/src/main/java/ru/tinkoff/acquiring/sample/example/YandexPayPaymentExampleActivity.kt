package ru.tinkoff.acquiring.sample.example

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.example.utils.getTerminalInfo
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.responses.Paymethod
import ru.tinkoff.acquiring.sdk.ui.activities.YandexPaymentLauncher
import ru.tinkoff.acquiring.sdk.utils.Money
import ru.tinkoff.acquiring.yandexpay.AcqYandexPayResult
import ru.tinkoff.acquiring.yandexpay.createYandexPayButtonFragment
import ru.tinkoff.acquiring.yandexpay.models.mapYandexPayData

/**
 *  * Пример запуска процесса оплаты Yandex Pay по нажатию на кнопку
 *
 * @author s.y.biryukov
 */
class YandexPayPaymentExampleActivity : AppCompatActivity() {

    /**
     * Лончер для запуска процесса оплаты
     */
    private val launcher =
        registerForActivityResult(YandexPaymentLauncher.Contract, ::handlePaymentResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.example_payment_yandex)

        val terminal = getTerminalInfo()
        AcquiringSdk.isDebug = true // Включение логгирования


        val tinkoffAcquiring = TinkoffAcquiring(
            context = this,
            terminalKey = terminal.terminalKey,
            publicKey = terminal.terminalPublicKey
        )

        tinkoffAcquiring.checkTerminalInfo( // Получение информации о доступных способах оплаты
            onSuccess = { terminalInfo ->
                val isYandexPayEnabled =
                    terminalInfo?.paymethods?.any { it.paymethod == Paymethod.YandexPay } == true
                val yandexPayData = terminalInfo?.mapYandexPayData()
                if (isYandexPayEnabled && yandexPayData != null) {
                    val paymentOptions = PaymentOptions().setOptions {
                        setTerminalParams(
                            terminalKey = terminal.terminalKey,
                            publicKey = terminal.terminalPublicKey
                        )
                        orderOptions {
                            orderId = "order_id" // Идентификатор заказа
                            amount = Money.ofRubles(10) // Сумма заказа
                        }
                    }

                    // Создаем фрагмент для кнопки
                    val buttonFragment =
                        tinkoffAcquiring.createYandexPayButtonFragment(
                            activity = this,
                            yandexPayData = yandexPayData,
                            options = paymentOptions,
                            onYandexSuccessCallback = {
                                startPayment(tinkoffAcquiring, paymentOptions, it)
                            }
                        )

                    supportFragmentManager.beginTransaction()
                        .add(R.id.button_fragment_container, buttonFragment)
                        .commit()
                }
            },
            onFailure = {
                Log.e(TAG, it.message, it)
            }
        )
    }

    private fun startPayment(
        tinkoffAcquiring: TinkoffAcquiring,
        paymentOptions: PaymentOptions,
        it: AcqYandexPayResult.Success
    ) {
        launcher.launch(
            YandexPaymentLauncher.Params(
                tinkoffAcquiring = tinkoffAcquiring,
                options = paymentOptions,
                yandexPayToken = it.token,
                paymentId = it.paymentOptions.paymentId
            )
        )
    }

    /**
     * Обработка результата
     */
    private fun handlePaymentResult(result: YandexPaymentLauncher.Result) {
        when (result) {
            YandexPaymentLauncher.Result.Cancelled -> {
                Toast.makeText(this, "Отменено", Toast.LENGTH_LONG).show()
            }

            is YandexPaymentLauncher.Result.Error -> {
                Toast.makeText(
                    this,
                    "Ошибка: ${result.error?.message} paymentId: ${result.paymentId}",
                    Toast.LENGTH_LONG
                ).show()
            }

            is YandexPaymentLauncher.Result.Success -> {
                Toast.makeText(
                    this,
                    "Платеж выполнен успешно.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        const val TAG = "SAMPLE"
    }
}
