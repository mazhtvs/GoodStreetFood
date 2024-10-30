package ru.tinkoff.acquiring.sdk.redesign.payment.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.databinding.AcqPaymentByCardNewActivityBinding
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_SAVED_CARDS
import ru.tinkoff.acquiring.sdk.redesign.payment.PaymentByCardLauncher.StartData
import ru.tinkoff.acquiring.sdk.utils.getParcelable
import ru.tinkoff.acquiring.sdk.utils.lazyUnsafe

/**
 * Created by i.golovachev
 */
internal class PaymentByCardActivity : AppCompatActivity() {

    private lateinit var binding: AcqPaymentByCardNewActivityBinding

    private val startData: StartData by lazyUnsafe {
        intent.getParcelable(EXTRA_SAVED_CARDS, StartData::class)!!
    }

    //region Activity LC
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcqPaymentByCardNewActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            view.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            insets
        }


        val paymentByCardFragment = PaymentByCardFragment().apply {
            arguments = bundleOf(
                PaymentByCardFragment.ARG_PAYMENT_CARDS_OPTION to startData
            )
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.acq_payment_by_card_root, paymentByCardFragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
