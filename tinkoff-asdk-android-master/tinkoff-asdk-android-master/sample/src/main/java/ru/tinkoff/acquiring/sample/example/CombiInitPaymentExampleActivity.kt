package ru.tinkoff.acquiring.sample.example

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tinkoff.acquiring.sample.databinding.ExamplePaymentCombiInitBinding
import ru.tinkoff.acquiring.sample.example.utils.TerminalData
import ru.tinkoff.acquiring.sample.example.utils.getTerminalInfo
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringApiException
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.payment.methods.InitConfigurator.configure
import ru.tinkoff.acquiring.sdk.redesign.mainform.MainFormLauncher
import ru.tinkoff.acquiring.sdk.utils.Money
import kotlin.random.Random

/**
 * Пример запуска процесса оплаты через платежную форму с использованием комбинированной инициализации.
 * 1. Получаем paymentId путем вызова метода [Init](https://www.tinkoff.ru/kassa/dev/payments/index.html#tag/Standartnyj-platyozh/paths/~1Init/post)
 * 2. Заппускаем платежную форму с передачей paymentId.
 *
 * @author s.y.biryukov
 */
class CombiInitPaymentExampleActivity : AppCompatActivity() {

    private var paymentIdFromServer: Long? = null
    private lateinit var orderId: String
    private lateinit var binding: ExamplePaymentCombiInitBinding
    private lateinit var terminal: TerminalData
    private lateinit var options: PaymentOptions

    /**
     * Лончер для запуска платежной формы
     */
    private val launcher =
        registerForActivityResult(MainFormLauncher.Contract, ::handlePaymentResult)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = ExamplePaymentCombiInitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AcquiringSdk.isDebug = true // Включение логгирования

        binding.btnBuyByCard.setOnClickListener { startPayment() }
        binding.btnGetPaymentId.setOnClickListener { loadPaymentIdFromServer() }

        initPayment()
    }

    private fun startPayment() {
        lifecycleScope.launch {
            try {
                val paymentId =
                    requireNotNull(paymentIdFromServer) { "Необходимо получить paymentId" }
                startPaymentWithPaymentId(options, paymentId) // Запуск оплаты с paymentId
            } catch (e: Throwable) {
                Log.e(TAG, e.message, e)
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@CombiInitPaymentExampleActivity,
                        e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadPaymentIdFromServer() {
        lifecycleScope.launch {
            val sdk = AcquiringSdk(
                terminalKey = terminal.terminalKey,
                publicKey = terminal.terminalPublicKey,
            )

            binding.statusText.isVisible = true
            binding.statusText.text = "Получение paymentId..."

            try {
                this@CombiInitPaymentExampleActivity.paymentIdFromServer =
                    getPaymentIdFromServer(sdk, options)
                binding.statusText.text = "Загружен paymentId: $paymentIdFromServer"
                binding.btnBuyByCard.text = "Оплатить с paymentId: $paymentIdFromServer"
            } catch (e: Throwable) {
                val errorCode = (e as? AcquiringApiException)?.response?.errorCode
                binding.statusText.text = "Ошибка: ${e.message} Код ошибки: $errorCode"
                Log.e(TAG, e.message, e)
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@CombiInitPaymentExampleActivity,
                        e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


        }
    }

    private fun initPayment() {
        val randomOrderId = Random(1000).nextInt().toString()
        this.orderId = randomOrderId
        this.terminal = getTerminalInfo() // Получение данных о терминале

        //<editor-fold desc="Формирование параметров платежа">
        this.options = PaymentOptions().setOptions {
            setTerminalParams(
                terminalKey = terminal.terminalKey,
                publicKey = terminal.terminalPublicKey
            )
            orderOptions {
                orderId = randomOrderId // Идентификатор заказа
                amount = Money.ofRubles(10) // Сумма заказа
            }
        }
        //</editor-fold>

        this.binding.title.text = "Заказ № $orderId"
    }


    /**
     * Получение paymentId с сервера.
     * Вызов Init из клиентского кода используется для демонстрации.
     */
    private suspend fun getPaymentIdFromServer(
        sdk: AcquiringSdk,
        options: PaymentOptions
    ) = withContext(Dispatchers.IO) {
        val paymentIdFomServer =
            sdk.init { configure(options) }.execute().paymentId
        requireNotNull(paymentIdFomServer) { "Что-то пошло не так, сервер не вернул paymentId" }
    }

    private fun startPaymentWithPaymentId(
        options: PaymentOptions,
        paymentId: Long
    ) {
        options.paymentId = paymentId // Берем paymentId из результата выполнения метода Init
        val startData = MainFormLauncher.StartData(
            paymentOptions = options,
        )
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

    companion object {
        private const val TAG = "SAMPLE"
    }
}
