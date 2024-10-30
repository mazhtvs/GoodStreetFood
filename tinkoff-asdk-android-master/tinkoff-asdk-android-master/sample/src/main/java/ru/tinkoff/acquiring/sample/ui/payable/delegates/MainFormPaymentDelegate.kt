package ru.tinkoff.acquiring.sample.ui.payable.delegates

import androidx.activity.result.ActivityResultLauncher
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.ui.payable.PayableActivity
import ru.tinkoff.acquiring.sample.utils.toast
import ru.tinkoff.acquiring.sdk.redesign.mainform.MainFormLauncher

/**
 * @author k.shpakovskiy
 */
interface MainFormPaymentDelegate {
    fun initMainFormPaymentDelegate(activity: PayableActivity)
    fun launchMainFormPayment()
}

class MainFormPayment : MainFormPaymentDelegate,
    InitRequestDelegate by InitRequestDelegateImpl() {
    private lateinit var activity: PayableActivity
    private lateinit var mainFormLauncher: ActivityResultLauncher<MainFormLauncher.StartData>

    override fun initMainFormPaymentDelegate(activity: PayableActivity) {
        this.activity = activity
        with(activity) {
            mainFormLauncher = registerForActivityResult(MainFormLauncher.Contract) { result ->
                when (result) {
                    is MainFormLauncher.Canceled -> toast("payment canceled")
                    is MainFormLauncher.Error -> {
                        toast(
                            "${result.error.message} ${result.paymentId} ${result.errorCode}"
                                ?: getString(R.string.error_title)
                        )
                    }

                    is MainFormLauncher.Success -> toast("payment Success-  paymentId:${result.paymentId}")
                }
            }
        }
    }

    override fun launchMainFormPayment() {
        with(activity) {
            val options = createPaymentOptions()
            if (settings.isEnableCombiInit) {
                startInitRequest(activity, options) { optionsWithPaymentId ->
                    mainFormLauncher.launch(MainFormLauncher.StartData(optionsWithPaymentId))
                }
            } else {
                mainFormLauncher.launch(MainFormLauncher.StartData(options))
            }
        }
    }
}
