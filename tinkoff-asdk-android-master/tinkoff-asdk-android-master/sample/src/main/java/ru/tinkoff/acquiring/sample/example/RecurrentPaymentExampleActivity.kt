package ru.tinkoff.acquiring.sample.example

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.SampleApplication
import ru.tinkoff.acquiring.sample.example.utils.createPaymentOptions
import ru.tinkoff.acquiring.sample.example.utils.createSavedCardOptions
import ru.tinkoff.acquiring.sample.ui.payable.delegates.RecurrentParentPayment
import ru.tinkoff.acquiring.sample.ui.payable.delegates.RecurrentParentPaymentDelegate
import ru.tinkoff.acquiring.sample.utils.toast
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.payment.RecurrentPaymentProcess
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ChooseCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.recurrent.RecurrentPayLauncher
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper

/**
 * Created by v.budnitskiy
 */
class RecurrentPaymentExampleActivity : AppCompatActivity(),
    RecurrentParentPaymentDelegate by RecurrentParentPayment() {

    // Лаунчер для запуска рекуррентного флоу
    private val recurrentPayment =
        registerForActivityResult(RecurrentPayLauncher.Contract) { result ->
            when (result) {
                is RecurrentPayLauncher.Canceled -> toast("payment canceled")
                is RecurrentPayLauncher.Error -> toast(
                    result.error.message ?: getString(R.string.error_title)
                )
                is RecurrentPayLauncher.Success -> toast("payment Success-  paymentId:${result.paymentId}")
            }
        }

    // Лаунчер для запуска выбора карт
    private val cardsForRecurrent =
        registerForActivityResult(ChooseCardLauncher.Contract) { result ->
            when (result) {
                is ChooseCardLauncher.Canceled -> Unit
                is ChooseCardLauncher.Error -> Unit
                // Выбранная карты
                is ChooseCardLauncher.CardSelected -> {
                    val card = result.card
                    val options = createPaymentOptions()
                    recurrentPayment.launch(
                        RecurrentPayLauncher.StartData(card, options)
                    )
                }
                else -> Unit
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example_payment_recurrent)

        AcquiringSdk.isDebug = true // Включение логгирования

        findViewById<TextView>(R.id.btn_buy_now).setOnClickListener { startPayment() }
    }

    private fun startPayment() {

        // Инициализация рекуррентного процесса
        with(this) {
            RecurrentPaymentProcess.init(
                sdk = SampleApplication.tinkoffAcquiring.sdk,
                application = application,
                threeDsDataCollector = ThreeDsHelper.CollectData
            )
            //
            val createSavedCardOptions = createSavedCardOptions(
                isRecurrent = true
            )
            // Запуск флоу выбора карты
            cardsForRecurrent.launch(
                ChooseCardLauncher.StartData(
                    savedCardsOptions = createSavedCardOptions,
                    paymentOptions = createPaymentOptions()
                )
            )
        }
    }
}
