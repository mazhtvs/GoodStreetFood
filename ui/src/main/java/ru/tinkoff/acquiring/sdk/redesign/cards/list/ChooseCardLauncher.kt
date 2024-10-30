package ru.tinkoff.acquiring.sdk.redesign.cards.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import kotlinx.android.parcel.Parcelize
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardsListActivity
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.RESULT_ERROR
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.RESULT_PAID_BY_NEW_CARD
import ru.tinkoff.acquiring.sdk.ui.activities.BaseAcquiringActivity.Companion.EXTRA_OPTIONS
import ru.tinkoff.acquiring.sdk.utils.getError
import ru.tinkoff.acquiring.sdk.utils.getExtra

/**
 * @author k.shpakovskiy
 */
object ChooseCardLauncher {
    sealed class Result

    class CardSelected(val card: Card) : Result()

    class Canceled : Result()

    class Error(val error: Throwable) : Result()

    class PaidByNewCard(
        val paymentId: Long? = null,
        val cardId: String? = null,
        val rebillId: String? = null
    ) : Result()

    @Parcelize
    data class StartData(
        val savedCardsOptions: SavedCardsOptions,
        val paymentOptions: PaymentOptions? = null,
    ) : Parcelable

    object Contract : ActivityResultContract<StartData, Result>() {
        internal const val EXTRA_CHOSEN_CARD = "extra_chosen_card"
        internal const val EXTRA_PAYMENT_OPTIONS = "extra_payment_options"

        override fun createIntent(context: Context, input: StartData): Intent {
            val options: SavedCardsOptions = input.savedCardsOptions
            options.validateRequiredFields()

            val intent = Intent(context, CardsListActivity::class.java)
            intent.putExtras(Bundle().apply {
                putParcelable(EXTRA_OPTIONS, options)
                putParcelable(EXTRA_PAYMENT_OPTIONS, input.paymentOptions)
            })
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Result = when (resultCode) {
            RESULT_OK ->
                CardSelected(
                    checkNotNull(
                        intent?.getExtra(EXTRA_CHOSEN_CARD, Card::class)
                    )
                )
            RESULT_ERROR -> Error(intent.getError())
            RESULT_PAID_BY_NEW_CARD -> PaidByNewCard(
                intent?.getLongExtra(LauncherConstants.EXTRA_PAYMENT_ID, -1),
                intent?.getStringExtra(LauncherConstants.EXTRA_CARD_ID),
                intent?.getStringExtra(LauncherConstants.EXTRA_REBILL_ID),
            )
            else -> Canceled()
        }
    }
}
