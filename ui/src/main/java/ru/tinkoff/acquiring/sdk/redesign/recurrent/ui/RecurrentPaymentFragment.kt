package ru.tinkoff.acquiring.sdk.redesign.recurrent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.databinding.AcqPaymentStatusFormBinding
import ru.tinkoff.acquiring.sdk.databinding.FragmentRecurrentPaymentBinding
import ru.tinkoff.acquiring.sdk.models.Card
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.redesign.common.cardpay.CardPayComponent
import ru.tinkoff.acquiring.sdk.redesign.dialog.PaymentStatusSheetState
import ru.tinkoff.acquiring.sdk.redesign.dialog.component.PaymentStatusComponent
import ru.tinkoff.acquiring.sdk.redesign.mainform.ui.BottomSheetComponent
import ru.tinkoff.acquiring.sdk.redesign.recurrent.presentation.RecurrentPaymentViewModel
import ru.tinkoff.acquiring.sdk.redesign.recurrent.presentation.RecurrentViewModelsFactory
import ru.tinkoff.acquiring.sdk.redesign.recurrent.presentation.RejectedViewModel
import ru.tinkoff.acquiring.sdk.redesign.recurrent.ui.RecurrentPaymentActivity.Companion.EXTRA_CARD
import ru.tinkoff.acquiring.sdk.smartfield.AcqTextFieldView
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper
import ru.tinkoff.acquiring.sdk.ui.activities.ThreeDsLauncher
import ru.tinkoff.acquiring.sdk.ui.customview.editcard.keyboard.SecureKeyboardController
import ru.tinkoff.acquiring.sdk.utils.lazyUnsafe
import ru.tinkoff.acquiring.sdk.utils.requestCvcFocus
import ru.tinkoff.acquiring.sdk.utils.setFlagNotTouchable
import ru.tinkoff.acquiring.sdk.utils.setFlagTouchable

class RecurrentPaymentFragment : Fragment() {

    private lateinit var viewBinding: FragmentRecurrentPaymentBinding

    private val paymentOptions: PaymentOptions by lazy {
        requireArguments().get(
            ARG_RECURRENT_PAYMENT_OPTION
        ) as PaymentOptions
    }
    private val factory by lazyUnsafe {
        RecurrentViewModelsFactory(
            requireActivity().application,
            paymentOptions
        )
    }
    private val recurrentPaymentViewModel: RecurrentPaymentViewModel by viewModels { factory }
    private val rejectedViewModel: RejectedViewModel by viewModels { factory }

    private val paymentStatusComponent by lazyUnsafe {
        PaymentStatusComponent(
            viewBinding = AcqPaymentStatusFormBinding.bind(requireActivity().findViewById(R.id.acq_payment_status)),
            onMainButtonClick = { recurrentPaymentViewModel.onClose() },
            onSecondButtonClick = { recurrentPaymentViewModel.onClose() },
        )
    }

    private val bottomSheetComponent by lazyUnsafe {
        BottomSheetComponent(viewBinding.root, viewBinding.acqRecurrentFormSheet) {
            recurrentPaymentViewModel.onClose()
        }
    }

    private val cardPayComponent by lazyUnsafe {
        CardPayComponent(
            viewBinding.root,
            viewBinding = viewBinding.acqRecurrentFormPay,
            email = null,
            onCvcCompleted = rejectedViewModel::inputCvc,
            onPayClick = rejectedViewModel::payRejected,
            useSecureKeyboard = paymentOptions.features.useSecureKeyboard,
        )
    }

    private val threeDsBrowserBasedLauncher = registerForActivityResult(ThreeDsLauncher.Contract) {
        when (it) {
            is ThreeDsLauncher.Result.Success -> {
                recurrentPaymentViewModel.set3dsResult(it.result as PaymentResult)
            }

            is ThreeDsLauncher.Result.Error -> {
                recurrentPaymentViewModel.set3dsResult(it.error)
            }

            ThreeDsLauncher.Result.Cancelled -> {
                recurrentPaymentViewModel.onClose()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            recurrentPaymentViewModel.onClose()
        }

        updatePaymentState()
        updateCvcValidState()
        updateLoadingState()
        updateForceHideKeyboard()
        subscribeOnEvents()
        viewBinding.root.post {
            bottomSheetComponent.onAttachedToWindow()
        }
        if (savedInstanceState == null) {
            recurrentPaymentViewModel.pay()
        }
    }

    override fun onResume() {
        super.onResume()
        val instance = SecureKeyboardController.getInstance()
        instance.registerInsets(requireView())
        instance.setKeyboardStateListener(object : SecureKeyboardController.KeyboardStateListener {
            override fun onPaddingUpdated(height: Int, navigationHeight: Int): Boolean {
                val acqRecurrentFormSheet = viewBinding.acqRecurrentFormSheet
                acqRecurrentFormSheet.updatePadding(bottom = height)
                lifecycleScope.launch {
                    delay(100)
                    bottomSheetComponent.trimSheetToContent(acqRecurrentFormSheet)
                }
                return true
            }
        })
    }

    override fun onPause() {
        super.onPause()
        SecureKeyboardController.getInstance().clear()
    }

    private fun updatePaymentState() = lifecycleScope.launch {
        combine(
            recurrentPaymentViewModel.state.filterNotNull(), rejectedViewModel.needInputCvcState
        ) { state, needCvcForCard -> state to needCvcForCard }
            .collectLatest { (state, needCvcForCard) ->
                val showPaymentNotifications = paymentOptions.features.showPaymentNotifications
                requireActivity().window.setFlagTouchable()
                if ((state is PaymentStatusSheetState.Error || state is PaymentStatusSheetState.Success)
                    && !showPaymentNotifications
                ) {
                    recurrentPaymentViewModel.onClose()
                    return@collectLatest
                } else if (state is PaymentStatusSheetState.Progress) {
                    requireActivity().window.setFlagNotTouchable()
                }

                paymentStatusComponent.isVisible = needCvcForCard == null
                paymentStatusComponent.render(state)
                cardPayComponent.isVisible(needCvcForCard != null)
                if (needCvcForCard != null) {
                    cardPayComponent.renderInputCvc(needCvcForCard, paymentOptions)
                    requestCvcFocus(viewBinding.root, cardPayComponent)
                    bottomSheetComponent.trimSheetToContent(viewBinding.acqRecurrentFormPayContainer)
                } else {
                    bottomSheetComponent.trimSheetToContent(paymentStatusComponent.viewBinding.root)
                }

            }
    }

    private fun requestCvcFocus(root: ViewGroup, card: CardPayComponent){
        root.findViewById<AcqTextFieldView>(R.id.cvc_input).requestCvcFocus(root, card)
    }

    private fun updateCvcValidState() = lifecycleScope.launch {
        rejectedViewModel.cvcValid.collectLatest {
            cardPayComponent.renderEnable(it)
        }
    }

    private fun updateLoadingState() = lifecycleScope.launch {
        rejectedViewModel.loaderButtonState.collectLatest { isLoading ->
            cardPayComponent.renderLoader(isLoading)
        }
    }

    private fun updateForceHideKeyboard() = lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            rejectedViewModel.needHideKeyboard.filter { it }.collectLatest {
                cardPayComponent.isKeyboardVisible(false)
            }
        }
    }

    private fun subscribeOnEvents() = lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            recurrentPaymentViewModel.events.collectLatest {
                when (it) {
                    is RecurrentPaymentEvent.CloseWithCancel -> {
                        setResult(RecurrentPaymentResult.CloseWithCancel())
                    }

                    is RecurrentPaymentEvent.CloseWithError -> {
                        setResult(RecurrentPaymentResult.CloseWithError(error = it.throwable))
                    }

                    is RecurrentPaymentEvent.CloseWithSuccess -> {
                        setResult(
                            RecurrentPaymentResult.CloseWithSuccess(
                                it.paymentId,
                                it.rebillId
                            )
                        )
                    }

                    is RecurrentPaymentEvent.To3ds -> {
                        tryLaunch3ds(it)
                    }
                }
            }
        }
    }

    private fun setResult(result: RecurrentPaymentResult) {
        parentFragmentManager.setFragmentResult(
            FRAGMENT_RESULT_KEY, bundleOf(
                FRAGMENT_RESULT_BUNDLE_KEY to result
            )
        )
    }

    private fun tryLaunch3ds(it: RecurrentPaymentEvent.To3ds) {
        try {
            lifecycleScope.launch {
                ThreeDsHelper.Launch(
                    activity = requireActivity(),
                    options = it.paymentOptions,
                    threeDsData = it.threeDsState.data,
                    browserBasedLauncher = threeDsBrowserBasedLauncher
                )
            }
        } catch (e: Throwable) {
            paymentStatusComponent.render(
                PaymentStatusSheetState.Error(
                    title = R.string.acq_commonsheet_failed_title,
                    mainButton = R.string.acq_commonsheet_failed_primary_button,
                    throwable = e
                )
            )
        } finally {
            recurrentPaymentViewModel.goTo3ds()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentRecurrentPaymentBinding.inflate(inflater, container, false)
            .also { this.viewBinding = it }
            .root
    }

    companion object {
        fun newInstance(paymentOptions: PaymentOptions, card: Card?): Fragment {
            return RecurrentPaymentFragment().apply {
                arguments = bundleOf(
                    ARG_RECURRENT_PAYMENT_OPTION to paymentOptions,
                    EXTRA_CARD to card
                )
            }
        }

        private val TAG = RecurrentPaymentFragment::class.simpleName
        internal val FRAGMENT_RESULT_KEY = "$TAG.FRAGMENT_RESULT_KEY"
        internal val FRAGMENT_RESULT_BUNDLE_KEY = "$TAG.FRAGMENT_RESULT_BUNDLE_KEY"
        internal const val ARG_RECURRENT_PAYMENT_OPTION = "ARG_RECURRENT_PAYMENT_OPTION"
    }
}

