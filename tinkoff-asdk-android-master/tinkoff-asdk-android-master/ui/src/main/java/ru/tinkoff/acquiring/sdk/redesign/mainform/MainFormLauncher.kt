package ru.tinkoff.acquiring.sdk.redesign.mainform

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import kotlinx.android.parcel.Parcelize
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringApiException
import ru.tinkoff.acquiring.sdk.exceptions.AcquiringSdkException
import ru.tinkoff.acquiring.sdk.exceptions.getErrorCodeIfApiError
import ru.tinkoff.acquiring.sdk.exceptions.getPaymentIdIfSdkError
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ChooseCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_CARD_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_ERROR
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_PAYMENT_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_REBILL_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.RESULT_ERROR
import ru.tinkoff.acquiring.sdk.redesign.common.result.AcqPaymentResult
import ru.tinkoff.acquiring.sdk.redesign.mainform.ui.MainPaymentFormActivity
import ru.tinkoff.acquiring.sdk.utils.getError
import ru.tinkoff.acquiring.sdk.utils.putOptions

object MainFormLauncher {
    sealed class Result
    class Success(
        override val paymentId: Long? = null,
        override val cardId: String? = null,
        override val rebillId: String? = null
    ) : Result(), AcqPaymentResult.Success

    object Canceled : Result(), AcqPaymentResult.Canceled
    class Error(
        override val error: Throwable,
        override val errorCode: Int?,
        override val paymentId: Long?
    ) : Result(), AcqPaymentResult.Error {

        constructor(error: AcquiringApiException) : this(
            error,
            error.response?.errorCode?.toInt(),
            null
        )
    }

    @Parcelize
    class StartData(val paymentOptions: PaymentOptions) : Parcelable

    object Contract : ActivityResultContract<StartData, Result>() {
        override fun createIntent(context: Context, input: StartData): Intent {
            return Intent(context, MainPaymentFormActivity::class.java).apply {
                putOptions(input.paymentOptions)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Result = when (resultCode) {
            RESULT_OK -> {
                checkNotNull(intent)
                Success(
                    intent.getLongExtra(EXTRA_PAYMENT_ID, -1),
                    intent.getStringExtra(EXTRA_CARD_ID),
                    intent.getStringExtra(EXTRA_REBILL_ID),
                )
            }

            RESULT_ERROR -> {

                val th = intent.getError()
                val errorCode = th.getErrorCodeIfApiError()?.toIntOrNull()
                val paymentId = th.getPaymentIdIfSdkError()

                Error(
                    error = th,
                    errorCode = errorCode,
                    paymentId = paymentId,
                )
            }

            else -> {
                Canceled
            }
        }

        internal fun createSuccessIntent(
            paymentId: Long? = null,
            cardId: String? = null,
            rebillId: String? = null
        ): Intent {
            val intent = Intent()
            intent.putExtra(EXTRA_PAYMENT_ID, paymentId)
            intent.putExtra(EXTRA_CARD_ID, cardId)
            intent.putExtra(EXTRA_REBILL_ID, rebillId)
            return intent
        }

        internal fun createSuccessIntent(
            paid: ChooseCardLauncher.PaidByNewCard
        ) = createSuccessIntent(paid.paymentId, paid.cardId, paid.cardId)

        internal fun createSuccessIntent(
            success: AcqPaymentResult.Success
        ) = createSuccessIntent(success.paymentId, success.cardId, success.cardId)

        internal fun createFailedIntent(throwable: Throwable): Intent {
            return Intent().putExtra(EXTRA_ERROR, throwable)
        }

        internal fun createFailedIntent(error: AcqPaymentResult.Error) =
            createFailedIntent(error.error)
    }
}
