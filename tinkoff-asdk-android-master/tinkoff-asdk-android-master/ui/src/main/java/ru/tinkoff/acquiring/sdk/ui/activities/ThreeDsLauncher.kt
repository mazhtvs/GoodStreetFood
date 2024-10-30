package ru.tinkoff.acquiring.sdk.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ru.tinkoff.acquiring.sdk.models.ThreeDsData
import ru.tinkoff.acquiring.sdk.models.options.screen.BaseAcquiringOptions
import ru.tinkoff.acquiring.sdk.models.result.AsdkResult
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper
import ru.tinkoff.acquiring.sdk.utils.getAsError
import ru.tinkoff.acquiring.sdk.utils.getExtra
import ru.tinkoff.acquiring.sdk.utils.getLongOrNull

/**
 * @author s.y.biryukov
 */
object ThreeDsLauncher {

    object Contract : ActivityResultContract<Params, Result>() {
        override fun createIntent(context: Context, input: Params): Intent {
            return ThreeDsActivity.createIntent(
                context = context,
                options = input.options,
                data = input.data,
                panSuffix = input.panSuffix ?: "",
            )
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            return when {
                resultCode == Activity.RESULT_OK && intent != null -> {
                   Result.Success(
                       result = intent.getExtra(ThreeDsHelper.Launch.RESULT_DATA, AsdkResult::class)!!
                   )
                }
                resultCode == ThreeDsHelper.Launch.RESULT_ERROR ->
                    Result.Error(
                        error = intent?.getAsError(ThreeDsHelper.Launch.ERROR_DATA) ?: Throwable(),
                        paymentId = intent?.getLongOrNull(LauncherConstants.EXTRA_PAYMENT_ID)
                    )
                else -> Result.Cancelled
            }
        }
    }

    class Params(
        val options: BaseAcquiringOptions,
        val data: ThreeDsData,
        val panSuffix: String?,
    )

    sealed class Result {

        class Success(
            val result: AsdkResult
        ) : Result()

        object Cancelled : Result()

        class Error(
            val error: Throwable,
            val paymentId: Long? = null,
        ) : Result()
    }
}
