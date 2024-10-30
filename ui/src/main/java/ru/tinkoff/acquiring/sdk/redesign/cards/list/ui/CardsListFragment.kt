package ru.tinkoff.acquiring.sdk.redesign.cards.list.ui

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import ru.tinkoff.acquiring.sdk.AcquiringSdk
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.databinding.AcqFragmentCardListBinding
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.redesign.cards.list.adapters.CardsListAdapter
import ru.tinkoff.acquiring.sdk.redesign.cards.list.presentation.CardsListFragmentViewModel
import ru.tinkoff.acquiring.sdk.redesign.common.util.AcqShimmerAnimator
import ru.tinkoff.acquiring.sdk.utils.AcqSnackBarHelper
import ru.tinkoff.acquiring.sdk.utils.BankCaptionResourceProvider
import ru.tinkoff.acquiring.sdk.utils.ConnectionChecker
import ru.tinkoff.acquiring.sdk.utils.checkNotNull
import ru.tinkoff.acquiring.sdk.utils.lazyUnsafe
import ru.tinkoff.acquiring.sdk.utils.lazyView
import ru.tinkoff.acquiring.sdk.utils.menuItemVisible
import ru.tinkoff.acquiring.sdk.utils.showById
import ru.tinkoff.acquiring.sdk.utils.showErrorDialog

/**
 * @author s.y.biryukov
 */
class CardsListFragment : Fragment() {
    private var viewBinding: AcqFragmentCardListBinding? = null
    private val viewModel: CardsListFragmentViewModel by viewModels {
        val savedCardOptions =
            requireArguments().getParcelable<SavedCardsOptions>(ARG_SAVED_CARDS_OPTION)
                .checkNotNull { "Не предан аргумент $ARG_SAVED_CARDS_OPTION" }

        CardsListFragmentViewModel.factory(
            AcquiringSdk(
                savedCardOptions.terminalKey,
                savedCardOptions.publicKey
            ),
            ConnectionChecker(requireContext()),
            BankCaptionResourceProvider(requireContext())
        )
    }
    private val recyclerView: RecyclerView by lazyView(R.id.acq_card_list_view)
    private val viewFlipper: ViewFlipper by lazyView(R.id.acq_view_flipper)
    private val cardShimmer: ViewGroup by lazyView(R.id.acq_card_list_shimmer)
    private val root: ViewGroup by lazyView(R.id.acq_card_list_base)
    private val stubImage: ImageView by lazyView(R.id.acq_stub_img)
    private val stubTitleView: TextView by lazyView(R.id.acq_stub_title)
    private val stubSubtitleView: TextView by lazyView(R.id.acq_stub_subtitle)
    private val stubButtonView: TextView by lazyView(R.id.acq_stub_retry_button)
    private val addNewCard: TextView by lazyView(R.id.acq_add_new_card)
    private val anotherCard: TextView by lazyView(R.id.acq_another_card)
    private lateinit var cardsListAdapter: CardsListAdapter

    private val snackBarHelper: AcqSnackBarHelper by lazyUnsafe {
        AcqSnackBarHelper(requireView().findViewById(R.id.acq_card_list_root))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return AcqFragmentCardListBinding.inflate(inflater, container, false)
            .also { this.viewBinding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initViews()
        subscribeOnState()
    }

    fun requestRefresh() {
        viewModel.onRequestRefresh()
    }

    private fun initToolbar() {
        val toolbar = viewBinding?.acqToolbar!!
        toolbar.setNavigationOnClickListener {
            viewModel.onBackPressed()
        }
        toolbar.inflateMenu(R.menu.acq_card_list_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.acq_card_list_action_change -> {
                    viewModel.switchToDeleteMode()
                    true
                }

                R.id.acq_card_list_action_complete -> {
                    viewModel.returnToBaseMode()
                    true
                }

                else -> false
            }
        }
    }

    private fun initViews() {
        cardsListAdapter = CardsListAdapter(
            onDeleteClick = { viewModel.deleteCard(it) },
            onChooseClick = { viewModel.chooseCard(it) }
        )
        recyclerView.adapter = cardsListAdapter
        addNewCard.setOnClickListener {
            viewModel.onAddNewCardClicked()
        }
        anotherCard.setOnClickListener {
            viewModel.onPayByAnotherCardClicked()
        }
    }

    private fun subscribeOnState() {
        lifecycleScope.launch {
            subscribeOnUiState()
            subscribeOnEvents()
            subscribeOnNavigation()
        }
    }

    private fun CoroutineScope.subscribeOnUiState() {
        launch {
            viewModel.stateUiFlow.collectLatest { state ->
                val toolbar = viewBinding!!.acqToolbar
                val mode = viewModel.stateUiFlow.value.menuMode
                val menu = toolbar.menu
                menu.menuItemVisible(R.id.acq_card_list_action_change, mode == MenuMode.EDIT)
                menu.menuItemVisible(R.id.acq_card_list_action_complete, mode == MenuMode.SUCCESS)

                if (state.screenMode == ScreenMode.PAYMENT) {
                    addNewCard.isVisible = false
                    anotherCard.isVisible = state.allowNewCard
                } else {
                    addNewCard.isVisible = state.allowNewCard
                    anotherCard.isVisible = false
                }

                if (state.withArrowBack) {
                    toolbar.setNavigationIcon(R.drawable.acq_arrow_back)
                }

                if (state.screenMode == ScreenMode.PAYMENT) {
                    toolbar.setTitle(R.string.acq_cardpay_title)
                } else {
                    toolbar.setTitle(R.string.acq_cardlist_title)
                }

                when (val listState = state.listState) {
                    is CardsListState.Content -> {
                        viewFlipper.showById(R.id.acq_card_list_content)
                        cardsListAdapter.setCards(listState.cards)
                    }

                    is CardsListState.Shimmer -> {
                        viewFlipper.showById(R.id.acq_card_list_shimmer)
                        AcqShimmerAnimator.animateSequentially(
                            cardShimmer.children.toList()
                        )
                    }

                    is CardsListState.Error -> {
                        showStub(
                            imageResId = R.drawable.acq_ic_generic_error_stub,
                            titleTextRes = R.string.acq_generic_alert_label,
                            subTitleTextRes = R.string.acq_generic_stub_description,
                            buttonTextRes = R.string.acq_generic_alert_access
                        )
                        stubButtonView.setOnClickListener { viewModel.onErrorButtonClick() }
                    }

                    is CardsListState.Empty -> {
                        val subTitleTextRes: Int
                        val buttonTextRes: Int
                        if (state.screenMode == ScreenMode.PAYMENT) {
                            subTitleTextRes = R.string.acq_cardlist_pay_description
                            buttonTextRes = R.string.acq_cardlist_pay_button_add
                            stubButtonView.setOnClickListener {
                                viewModel.onPayByAnotherCardClicked()
                            }
                        } else {
                            subTitleTextRes = R.string.acq_cardlist_description
                            buttonTextRes = R.string.acq_cardlist_button_add
                            stubButtonView.setOnClickListener {
                                viewModel.onAddNewCardClicked()
                            }
                        }
                        showStub(
                            imageResId = R.drawable.acq_ic_cards_list_empty,
                            titleTextRes = null,
                            subTitleTextRes = subTitleTextRes,
                            buttonTextRes = buttonTextRes,
                            showButton = state.allowNewCard,
                        )
                    }

                    is CardsListState.NoNetwork -> {
                        showStub(
                            imageResId = R.drawable.acq_ic_no_network,
                            titleTextRes = R.string.acq_generic_stubnet_title,
                            subTitleTextRes = R.string.acq_generic_stubnet_description,
                            buttonTextRes = R.string.acq_generic_button_stubnet
                        )
                        stubButtonView.setOnClickListener {
                            viewModel.onReloadButtonClick()
                        }
                    }
                }
            }
        }
    }

    private fun CoroutineScope.subscribeOnNavigation() {
        launch {
            viewModel.navigationFlow.collectLatest {
                when (it) {
                    is CardListNav.ToAttachCard -> setAttackCardResult()
                    is CardListNav.ToPayByNewCard -> setPayByNewCardResult()
                    is CardListNav.FinishWithError -> setErrorResult(it.throwable)
                    is CardListNav.FinishWithSelectCard -> finishWithCard(it.selectedCard)
                    is CardListNav.FinishWithCancel -> setCancelResult()
                }
            }
        }
    }

    private fun CoroutineScope.subscribeOnEvents() {
        launch {
            viewModel.eventFlow.filterNotNull().collect {
                when (it) {
                    is CardListEvent.RemoveCardProgress -> showRemoveCardProgress(it.deletedCard.tail)
                    is CardListEvent.RemoveCardSuccess -> {
                        hideProgress()
                    }

                    is CardListEvent.ShowError -> {
                        hideProgress()
                        requireContext().showErrorDialog(
                            R.string.acq_generic_alert_label,
                            R.string.acq_generic_stub_description,
                            R.string.acq_generic_alert_access
                        )
                    }

                    is CardListEvent.ShowCardDeleteError -> {
                        hideProgress()
                        requireContext().showErrorDialog(
                            R.string.acq_cardlist_alert_deletecard_label,
                            null,
                            R.string.acq_generic_alert_access
                        )
                    }

                    is CardListEvent.ShowCardAttachDialog -> {
                        snackBarHelper.showWithIcon(
                            R.drawable.acq_ic_card_sparkle,
                            getString(R.string.acq_cardlist_snackbar_add, it.it)
                        )
                    }
                }
            }
        }
    }

    private fun showStub(
        imageResId: Int,
        titleTextRes: Int?,
        subTitleTextRes: Int,
        buttonTextRes: Int,
        showButton: Boolean = true
    ) {
        viewFlipper.showById(R.id.acq_card_list_stub)

        stubImage.setImageResource(imageResId)
        if (titleTextRes == null) {
            stubTitleView.visibility = View.GONE
        } else {
            stubTitleView.setText(titleTextRes)
            stubTitleView.visibility = View.VISIBLE
        }
        stubSubtitleView.setText(subTitleTextRes)

        stubButtonView.isVisible = showButton
        stubButtonView.setText(buttonTextRes)
    }

    private fun showRemoveCardProgress(cardTail: String?) {
        root.alpha = 0.5f
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        snackBarHelper.showProgress(
            getString(
                R.string.acq_cardlist_snackbar_remove_progress,
                cardTail
            )
        )
    }

    private fun hideProgress() {
        root.alpha = 1f
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        snackBarHelper.hide(SNACK_BAR_HIDE_DELAY)
    }

    private fun finishWithCard(card: Card) {
        setResult(Result.CardSelected(card))
    }

    private fun setPayByNewCardResult() {
        setResult(Result.PayByNewCard)
    }

    private fun setAttackCardResult() {
        setResult(Result.AddCard)
    }

    private fun setErrorResult(error: Throwable, errorCode: Int? = null) {
        setResult(
            Result.Error(
                error = error,
                errorCode = errorCode,
            )
        )
    }

    private fun setCancelResult() {
        setResult(Result.Cancelled)
    }

    private fun setResult(addCard: Result) {
        parentFragmentManager.setFragmentResult(
            FRAGMENT_RESULT_KEY, bundleOf(
                FRAGMENT_RESULT_BUNDLE_KEY to addCard
            )
        )
    }

    companion object {
        private val tag = CardsListFragment::class.simpleName

        private const val SNACK_BAR_HIDE_DELAY = 500L

        const val ARG_SAVED_CARDS_OPTION = "ARG_SAVED_CARDS_OPTION"

        val FRAGMENT_RESULT_KEY = "$tag.FRAGMENT_RESULT_KEY"
        val FRAGMENT_RESULT_BUNDLE_KEY = "$tag.FRAGMENT_RESULT_BUNDLE_KEY"
    }


    sealed interface Result : Parcelable {
        @Parcelize
        data class CardSelected(val card: Card) : Result

        @Parcelize
        data object Cancelled : Result

        @Parcelize
        data class Error(
            val error: Throwable,
            val errorCode: Int? = null,
        ) : Result

        @Parcelize
        data object AddCard : Result

        @Parcelize
        data object PayByNewCard : Result

    }
}
