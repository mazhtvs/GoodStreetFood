package ru.tinkoff.acquiring.sdk.redesign.cards.list.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.options.screen.AttachCardOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.redesign.cards.attach.AttachCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ChooseCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardListActivityViewModel.CardListFlowCommand
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ui.CardListActivityViewModel.CardListFlowState
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants
import ru.tinkoff.acquiring.sdk.redesign.mainform.MainFormLauncher
import ru.tinkoff.acquiring.sdk.redesign.payment.PaymentByCardLauncher
import ru.tinkoff.acquiring.sdk.ui.activities.TransparentActivity
import ru.tinkoff.acquiring.sdk.utils.AcqSnackBarHelper
import ru.tinkoff.acquiring.sdk.utils.ErrorResolver
import ru.tinkoff.acquiring.sdk.utils.lazyUnsafe

internal class CardsListActivity : TransparentActivity() {

    private val viewModel: CardListActivityViewModel by viewModels()

    private val snackBarHelper: AcqSnackBarHelper by lazyUnsafe {
        AcqSnackBarHelper(findViewById(R.id.acq_card_list_root))
    }

    private val attachCard = registerForActivityResult(AttachCardLauncher.Contract) { result ->
        when (result) {
            is AttachCardLauncher.Success -> onAttachCardSuccess(result)
            is AttachCardLauncher.Error -> onAttachCardError(result)
            is AttachCardLauncher.Canceled -> Unit
        }
    }

    private val byNewCardPayment =
        registerForActivityResult(PaymentByCardLauncher.Contract) { result ->
            when (result) {
                is PaymentByCardLauncher.Success -> onPayByNewCardSuccess(result)
                is PaymentByCardLauncher.Error -> onPayByNewCardError(result)
                is PaymentByCardLauncher.Canceled -> Unit
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.acq_activity_card_list)

        lifecycleScope.launchWhenResumed {
            handleState()
        }

        lifecycleScope.launchWhenResumed {
            handleCommands()
        }
    }

    private suspend fun handleCommands() {
        viewModel.commandFlow.collect {
            when (it) {
                is CardListFlowCommand.OpenAttachCard -> startAttachCard(
                    it.attachCardOptions
                )

                is CardListFlowCommand.OpenPayByNewCard -> payByNewCard(
                    it.paymentOptions
                )

                is CardListFlowCommand.ShowCardAddSuccess -> showAddCardSuccess(
                    it.panSuffix
                )

                CardListFlowCommand.RefreshCardList -> refreshCardList()
            }
        }
    }

    private suspend fun handleState() {
        viewModel.stateFlow.collect {
            when (it) {
                is CardListFlowState.OpenCardsList -> showCardListScreen(it.savedCardsOptions)
            }
        }
    }

    private fun refreshCardList() {
        (supportFragmentManager.findFragmentByTag(CARD_LIST_FRAGMENT_TAG) as? CardsListFragment)
            ?.requestRefresh()
    }

    private fun showAddCardSuccess(panSuffix: String) {
        snackBarHelper.showWithIcon(
            R.drawable.acq_ic_card_sparkle,
            getString(R.string.acq_cardlist_snackbar_add, panSuffix)
        )
    }

    private fun showCardListScreen(
        savedCardOptions: SavedCardsOptions
    ) {
        val cardListFragment = CardsListFragment().apply {
            arguments = bundleOf(
                CardsListFragment.ARG_SAVED_CARDS_OPTION to savedCardOptions
            )
        }

        supportFragmentManager.setFragmentResultListener(
            CardsListFragment.FRAGMENT_RESULT_KEY, this
        ) { key, bundle ->
            val result =
                bundle.getParcelable<CardsListFragment.Result>(CardsListFragment.FRAGMENT_RESULT_BUNDLE_KEY)

            when (result) {
                CardsListFragment.Result.Cancelled -> finishWithCancel()
                is CardsListFragment.Result.CardSelected -> finishWithCard(
                    card = result.card
                )

                is CardsListFragment.Result.Error -> finishWithError(
                    throwable = result.error
                )

                CardsListFragment.Result.AddCard -> viewModel.onAttachCard()
                CardsListFragment.Result.PayByNewCard -> viewModel.onPayByNewCard()
                null -> Unit
            }
        }

        supportFragmentManager.beginTransaction()
            .add(R.id.acq_card_list_root, cardListFragment, CARD_LIST_FRAGMENT_TAG)
            .commit()
    }

    private fun payByNewCard(paymentOptions: PaymentOptions) {
        val startData = PaymentByCardLauncher.StartData(
            paymentOptions = paymentOptions,
            cards = emptyList(),
            withArrowBack = true,
        )
        byNewCardPayment.launch(startData)
    }

    private fun startAttachCard(attachCardOptions: AttachCardOptions) {
        attachCard.launch(attachCardOptions)
    }

    private fun finishWithCard(card: Card) {
        setResult(RESULT_OK, Intent().putExtra(ChooseCardLauncher.Contract.EXTRA_CHOSEN_CARD, card))
        super.finish()
    }

    private fun onAttachCardError(result: AttachCardLauncher.Error) {
        showErrorDialog(
            getString(R.string.acq_generic_alert_label), ErrorResolver.resolve(
                result.error, getString(R.string.acq_generic_stub_description)
            ), getString(R.string.acq_generic_alert_access)
        )
    }

    private fun onAttachCardSuccess(result: AttachCardLauncher.Success) {
        viewModel.onAttachCardSuccess(
            cardId = result.cardId, panSuffix = result.panSuffix
        )
    }

    private fun onPayByNewCardSuccess(it: PaymentByCardLauncher.Success) {
        setResult(
            LauncherConstants.RESULT_PAID_BY_NEW_CARD,
            MainFormLauncher.Contract.createSuccessIntent(it)
        )
        finish()
    }

    private fun onPayByNewCardError(it: PaymentByCardLauncher.Error) {
        finishWithError(
            throwable = it.error,
            paymentId = it.paymentId
        )
    }

    companion object {
        const val CARD_LIST_FRAGMENT_TAG = "CARD_LIST_FRAGMENT_TAG"
    }
}
