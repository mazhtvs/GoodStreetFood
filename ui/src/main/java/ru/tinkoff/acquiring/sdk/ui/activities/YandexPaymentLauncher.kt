package ru.tinkoff.acquiring.sdk.ui.activities

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.models.YandexPayState
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants
import ru.tinkoff.acquiring.sdk.utils.getAsError
import ru.tinkoff.acquiring.sdk.utils.getLongOrNull

/**
 * @author s.y.biryukov
 */
object YandexPaymentLauncher {

    object Contract : ActivityResultContract<Params, Result>() {
        override fun createIntent(context: Context, input: Params): Intent {
            val options = input.options
            options.asdkState = YandexPayState(input.yandexPayToken, input.paymentId)
            input.tinkoffAcquiring.addKeys(options)
            val intent =
                BaseAcquiringActivity.createIntent(context, options, YandexPaymentActivity::class)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            return when (resultCode) {
                AppCompatActivity.RESULT_OK -> Result.Success
                LauncherConstants.RESULT_ERROR -> Result.Error(
                    error = intent?.getAsError(LauncherConstants.EXTRA_ERROR),
                    paymentId = intent?.getLongOrNull(LauncherConstants.EXTRA_PAYMENT_ID)
                )
                else -> Result.Cancelled
            }
        }

    }

    class Params(
        val tinkoffAcquiring: TinkoffAcquiring,
        val options: PaymentOptions,
        val yandexPayToken: String,
        val paymentId: Long? = null
    )

    sealed class Result {

        object Success : Result()

        object Cancelled : Result()

        class Error(
            val error: Throwable? = null,
            val paymentId: Long? = null,
        ) : Result()
    }
}
