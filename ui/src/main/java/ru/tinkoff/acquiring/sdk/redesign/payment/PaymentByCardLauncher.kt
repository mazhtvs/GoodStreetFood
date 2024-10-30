package ru.tinkoff.acquiring.sdk.redesign.payment

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import kotlinx.android.parcel.Parcelize
import ru.tinkoff.acquiring.sdk.exceptions.getErrorCodeIfApiError
import ru.tinkoff.acquiring.sdk.exceptions.getPaymentIdIfSdkError
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_CARD_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_PAYMENT_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_REBILL_ID
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.EXTRA_SAVED_CARDS
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.RESULT_ERROR
import ru.tinkoff.acquiring.sdk.redesign.common.result.AcqPaymentResult
import ru.tinkoff.acquiring.sdk.redesign.payment.ui.PaymentByCardActivity
import ru.tinkoff.acquiring.sdk.utils.getError

object PaymentByCardLauncher {

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
    ) : Result(), AcqPaymentResult.Error

    @Parcelize
    class StartData(
        val paymentOptions: PaymentOptions,
        val cards: List<Card>,
        val withArrowBack: Boolean = false
    ) : Parcelable

    object Contract : ActivityResultContract<StartData, Result>() {

        internal fun createSuccessIntent(paymentResult: PaymentResult): Intent {
            val intent = Intent()
            intent.putExtra(EXTRA_PAYMENT_ID, paymentResult.paymentId)
            intent.putExtra(EXTRA_CARD_ID, paymentResult.cardId)
            intent.putExtra(EXTRA_REBILL_ID, paymentResult.rebillId)
            return intent
        }

        override fun createIntent(context: Context, input: StartData): Intent =
            Intent(context, PaymentByCardActivity::class.java).apply {
                putExtra(EXTRA_SAVED_CARDS, input)
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
                val error = intent.getError()
                Error(
                    error = error,
                    errorCode = error.getErrorCodeIfApiError()?.toIntOrNull(),
                    paymentId = error.getPaymentIdIfSdkError()
                )
            }
            else -> Canceled
        }
    }
}
