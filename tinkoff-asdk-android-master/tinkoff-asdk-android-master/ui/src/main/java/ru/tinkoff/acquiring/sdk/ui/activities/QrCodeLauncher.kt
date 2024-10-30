package ru.tinkoff.acquiring.sdk.ui.activities

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.models.options.FeaturesOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.BaseAcquiringOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants
import ru.tinkoff.acquiring.sdk.utils.getAsError
import ru.tinkoff.acquiring.sdk.utils.getLongOrNull

object QrCodeLauncher {

    object Contract : ActivityResultContract<Params, Result>() {
        override fun createIntent(context: Context, input: Params): Intent {
            val paymentOptions = input.paymentOptions
            val featureOptions = input.featureOptions
            val options = when {
                paymentOptions != null -> paymentOptions
                featureOptions != null -> BaseAcquiringOptions().apply { features = featureOptions }
                else -> error("paymentOptions or featureOptions must be not null")
            }
            val optionsWithKeys = input.tinkoffAcquiring.addKeys(options)
            return BaseAcquiringActivity.createIntent(
                context,
                optionsWithKeys,
                QrCodeActivity::class
            )
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
        val paymentOptions: PaymentOptions? = null,
        val featureOptions: FeaturesOptions? = null,
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
