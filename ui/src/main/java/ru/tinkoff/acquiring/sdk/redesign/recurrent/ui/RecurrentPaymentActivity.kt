package ru.tinkoff.acquiring.sdk.redesign.recurrent.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.databinding.AcqRecurrentFromActivityBinding
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants
import ru.tinkoff.acquiring.sdk.redesign.recurrent.RecurrentPayLauncher
import ru.tinkoff.acquiring.sdk.utils.getExtra
import ru.tinkoff.acquiring.sdk.utils.getOptions
import ru.tinkoff.acquiring.sdk.utils.lazyUnsafe
import ru.tinkoff.acquiring.sdk.utils.putOptions

/**
 * Created by i.golovachev
 */
// TODO Вынести навигацию в отдельный класс.
internal class RecurrentPaymentActivity : AppCompatActivity() {

    private val paymentOptions by lazyUnsafe { intent.getOptions<PaymentOptions>() }
    private lateinit var binding: AcqRecurrentFromActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        bindView()
        setFragmentResultListener()
        val paymentByCardFragment = RecurrentPaymentFragment.newInstance(
            paymentOptions, intent.getExtra(EXTRA_CARD, Card::class)
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.acq_main_form_root, paymentByCardFragment)
            .commit()
    }

    private fun bindView() {
        binding = AcqRecurrentFromActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setFragmentResultListener(){
        supportFragmentManager.setFragmentResultListener(
            RecurrentPaymentFragment.FRAGMENT_RESULT_KEY, this
        ) { key, bundle ->
            val result =
                bundle.getParcelable<RecurrentPaymentResult>(RecurrentPaymentFragment.FRAGMENT_RESULT_BUNDLE_KEY)

            when (result) {
                is RecurrentPaymentResult.CloseWithCancel -> {
                    setResult(RESULT_CANCELED)
                    finish()
                }
                is RecurrentPaymentResult.CloseWithError -> {
                    setResult(
                        LauncherConstants.RESULT_ERROR,
                        RecurrentPayLauncher.Contract.createFailedIntent(result.error)
                    )
                    finish()
                }
                is RecurrentPaymentResult.CloseWithSuccess -> {
                    setResult(
                        RESULT_OK,
                        RecurrentPayLauncher.Contract.createSuccessIntent(result.paymentId, result.rebillId)
                    )
                    finish()
                }
                else -> {}
            }
        }
    }

    companion object {

        internal const val EXTRA_CARD = "extra_card"

        fun intent(context: Context, paymentOptions: PaymentOptions, card: Card): Intent {
            return Intent(context, RecurrentPaymentActivity::class.java).apply {
                putOptions(paymentOptions)
                putExtra(EXTRA_CARD, card)
            }
        }
    }
}
