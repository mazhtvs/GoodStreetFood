package ru.tinkoff.acquiring.sdk.redesign.payment.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.databinding.AcqFragmentPaymentByCardBinding
import ru.tinkoff.acquiring.sdk.models.options.screen.SavedCardsOptions
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.payment.PaymentByCardState
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ChooseCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants
import ru.tinkoff.acquiring.sdk.redesign.common.carddatainput.CardDataInputFragment
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.EmailInputFragment
import ru.tinkoff.acquiring.sdk.redesign.dialog.OnPaymentSheetCloseListener
import ru.tinkoff.acquiring.sdk.redesign.dialog.PaymentStatusSheetState
import ru.tinkoff.acquiring.sdk.redesign.dialog.createPaymentSheetWrapper
import ru.tinkoff.acquiring.sdk.redesign.dialog.getPaymentResult
import ru.tinkoff.acquiring.sdk.redesign.dialog.showIfNeed
import ru.tinkoff.acquiring.sdk.redesign.payment.PaymentByCardLauncher
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper
import ru.tinkoff.acquiring.sdk.ui.activities.ThreeDsLauncher
import ru.tinkoff.acquiring.sdk.ui.component.bindKtx
import ru.tinkoff.acquiring.sdk.ui.customview.LoaderButton
import ru.tinkoff.acquiring.sdk.ui.customview.editcard.keyboard.SecureKeyboardController
import ru.tinkoff.acquiring.sdk.utils.UiLogger
import ru.tinkoff.acquiring.sdk.utils.checkNotNull
import ru.tinkoff.acquiring.sdk.utils.getParcelable
import ru.tinkoff.acquiring.sdk.utils.lazyUnsafe
import ru.tinkoff.acquiring.sdk.utils.lazyView
import ru.tinkoff.acquiring.sdk.utils.setFlagNotTouchable
import ru.tinkoff.acquiring.sdk.utils.setFlagTouchable

/**
 * Created by v.budnitskiy
 */
internal class PaymentByCardFragment: Fragment(),
    CardDataInputFragment.OnCardDataChanged,
    EmailInputFragment.OnEmailDataChanged,
    OnPaymentSheetCloseListener{

    private var viewBinding: AcqFragmentPaymentByCardBinding? = null

    companion object {
        val logger = UiLogger()
        const val ARG_PAYMENT_CARDS_OPTION = "ARG_SAVED_CARDS_OPTION"
    }

    private val startData: PaymentByCardLauncher.StartData by lazyUnsafe {
        requireActivity().intent.getParcelable(LauncherConstants.EXTRA_SAVED_CARDS, PaymentByCardLauncher.StartData::class)!!
    }

    private val savedCardOptions: SavedCardsOptions by lazyUnsafe {
        SavedCardsOptions().apply {
            setTerminalParams(
                startData.paymentOptions.terminalKey,
                startData.paymentOptions.publicKey
            )
            customer = startData.paymentOptions.customer
            features = startData.paymentOptions.features
            mode = SavedCardsOptions.Mode.PAYMENT
            withArrowBack = true
        }
    }

    private val isCardSaved: Boolean by lazy {
        viewModel.isCardSavedFocusChanger()
    }

    private val cardDataInputContainer: FragmentContainerView by lazyView(R.id.fragment_card_data_input)
    private val cardDataInput
        get() = childFragmentManager.findFragmentById(R.id.fragment_card_data_input) as CardDataInputFragment
    private val chosenCardContainer: CardView by lazyView(R.id.acq_chosen_card)
    private val chosenCardComponent: ChosenCardComponent by lazyUnsafe {
        ChosenCardComponent(
            chosenCardContainer,
            isCardSaved,
            onChangeCard = { onChangeCard() },
            onCvcCompleted = { cvc, isValid -> viewModel.setCvc(cvc, isValid) },
            useSecureKeyboard = startData.paymentOptions.features.useSecureKeyboard,
        )
    }
    private val emailInput: EmailInputFragment by lazyUnsafe {
        EmailInputFragment.getInstance(startData.paymentOptions.customer.email)
    }
    private val emailInputContainer: FragmentContainerView by lazyView(R.id.fragment_email_input)
    private val sendReceiptSwitch: SwitchCompat by lazyView(R.id.acq_send_receipt_switch)
    private val payButton: LoaderButton by lazyView(R.id.acq_pay_btn)
    private val viewModel: PaymentByCardViewModel by viewModels {
        PaymentByCardViewModel.factory(
            application = requireActivity().application,
            paymentOptions = requireArguments().getParcelable<PaymentByCardLauncher.StartData>(ARG_PAYMENT_CARDS_OPTION)
                .checkNotNull { "args null ${ARG_PAYMENT_CARDS_OPTION}" }.paymentOptions
        )
    }

    private val statusSheetStatus = createPaymentSheetWrapper()
    private val savedCards =
        registerForActivityResult(ChooseCardLauncher.Contract) { result ->
            when (result) {
                is ChooseCardLauncher.CardSelected -> {
                    viewModel.setSavedCard(result.card)
                    if (viewModel.state.value.cvc == null) {
                        chosenCardComponent.clearInput()
                    }
                }

                is ChooseCardLauncher.Canceled -> {
                    viewModel.setInputNewCard()
                    cardDataInput.clearInput()
                    cardDataInput.cardNumberInput.requestViewFocus()
                }

                is ChooseCardLauncher.Error -> {
                    logger.log(result.error)
                    onClose(PaymentStatusSheetState.Error(throwable = result.error))
                }

                is ChooseCardLauncher.PaidByNewCard -> onClose(PaymentStatusSheetState.Success(
                    paymentId = result.paymentId!!,
                    cardId = result.cardId,
                    rebillId = result.rebillId,
                ))
            }
        }

    private val threeDsBrowserBasedLauncher = registerForActivityResult(ThreeDsLauncher.Contract) {
        when (it) {
            is ThreeDsLauncher.Result.Success -> {
                val result = it.result as PaymentResult
                handleFinalState(
                    PaymentStatusSheetState.Success(
                        title = R.string.acq_commonsheet_paid_title,
                        mainButton = R.string.acq_commonsheet_clear_primarybutton,
                        paymentId = result.paymentId!!,
                        cardId = result.cardId,
                        rebillId = result.rebillId
                    )
                )
            }

            is ThreeDsLauncher.Result.Error -> {
                handleLoadingInProcess(false)

                val state = PaymentStatusSheetState.Error(
                    title = R.string.acq_commonsheet_failed_title,
                    mainButton = R.string.acq_commonsheet_clear_primarybutton,
                    throwable = it.error
                )
                handleFinalState(state)
            }

            ThreeDsLauncher.Result.Cancelled -> {
                handleLoadingInProcess(false)
            }
        }
    }

    //todo сделать через вьюмодельй
    private var onBackEnabled: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return AcqFragmentPaymentByCardBinding.inflate(inflater, container, false)
            .also { this.viewBinding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initViews()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (onBackEnabled.not()) return@addCallback
            viewModel.cancelPayment()
            finishWithCancel()
        }

        chosenCardComponent.clearCvc()
        lifecycleScope.launchWhenResumed { processState() }
        lifecycleScope.launchWhenCreated { uiState() }
        lifecycleScope.launch { selectedCardState() }


        cardDataInput.setupCameraCardScanner(startData.paymentOptions.features.cameraCardScannerContract)
        cardDataInput.useSecureKeyboard = startData.paymentOptions.features.useSecureKeyboard
        chosenCardComponent.bindKtx(lifecycleScope, viewModel.state.mapNotNull { it.chosenCard })
    }

    override fun onStop() {
        super.onStop()
        statusSheetStatus.takeIf { it.isAdded }?.dismissAllowingStateLoss()
        payButton.setOnClickListener {
            viewModel.pay()
        }
    }

    override fun onResume() {
        super.onResume()
        if (statusSheetStatus.state != null) {
            statusSheetStatus.showIfNeed(childFragmentManager)
        }
        viewBinding?.let {
            SecureKeyboardController.getInstance().apply {
                setContentContainer(it.acqPayByCardContentWrapper)
                registerInsets(it.root)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        SecureKeyboardController.getInstance().clear()
    }

    override fun onDestroyView() {
        viewBinding = null
        super.onDestroyView()
    }
    //endregion

    //region Data change callbacks
    override fun onCardDataChanged(isValid: Boolean) {
        viewModel.setCardData(
            cardNumber = cardDataInput.cardNumber,
            cvc = cardDataInput.cvc,
            dateExpired = cardDataInput.expiryDate,
            isValidCardData = isValid
        )
    }

    override fun onEmailDataChanged(email: String, isValid: Boolean) {
        viewModel.setEmail(email = email, isValidEmail = isValid)
    }
    //endregion

    //region Navigation
    private fun onChangeCard() {
        viewModel.rechoseCard()
        savedCards.launch(
            ChooseCardLauncher.StartData(
                savedCardOptions,
                startData.paymentOptions
            )
        )
    }

    override fun onClose(state: PaymentStatusSheetState) {
        when (state) {
            is PaymentStatusSheetState.Error -> finishWithError(state)
            is PaymentStatusSheetState.Success -> finishWithSuccess(state.getPaymentResult())
            else -> finishWithCancel()
        }
    }

    private fun finishWithCancel() {
        requireActivity().setResult(AppCompatActivity.RESULT_CANCELED)
        requireActivity().finish()
    }

    private fun finishWithError(state: PaymentStatusSheetState.Error) {
        requireActivity().setResult(LauncherConstants.RESULT_ERROR, Intent().putExtra(LauncherConstants.EXTRA_ERROR, state.throwable))
        requireActivity().finish()
    }
    //endregion

    //region init views
    private fun initToolbar() {
        val toolbar = viewBinding?.acqToolbar
        val activity = (requireActivity() as? PaymentByCardActivity)
        activity?.setSupportActionBar(toolbar)
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.setDisplayShowHomeEnabled(true)
        activity?.supportActionBar?.setTitle(R.string.acq_cardpay_title)
        if (startData.withArrowBack) {
            toolbar?.setNavigationIcon(R.drawable.acq_arrow_back)
        }
    }

    private fun initViews() {

        childFragmentManager.commit {
            replace(emailInputContainer.id, emailInput)
        }

        sendReceiptSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.sendReceiptChange(isChecked)
            // TODO Избавиться от логики на ui в рамках задачи EACQAPW-9367
            when {
                emailInput.isAdded.not() || emailInput.isVisible.not() -> return@setOnCheckedChangeListener
                isChecked -> emailInput.viewModel.requestEmailInputFocus()
                else -> {
                    emailInput.viewModel.clearEmailInputFocus()
                    emailInput.viewModel.hideEmailInputKeyboard()
                    if (cardDataInput.isAdded && cardDataInput.isVisible) {
                        cardDataInput.clearFocus()
                    }
                }
            }
        }

        payButton.setOnClickListener {
            viewModel.pay()
        }
    }
    //endregion

    //region subscribe States
    private suspend fun uiState() {
        viewModel.state.collect {
            chosenCardContainer.isVisible = it.chosenCard != null
            cardDataInputContainer.isVisible = it.chosenCard == null
            emailInputContainer.isVisible = it.sendReceipt
            sendReceiptSwitch.isChecked = it.sendReceipt
            payButton.text = getString(R.string.acq_cardpay_pay, it.amount)

            payButton.isEnabled = it.buttonEnabled
        }
    }

    private fun handleFinalState(state: PaymentStatusSheetState) {
        if (startData.paymentOptions.features.showPaymentNotifications) {
            statusSheetStatus.showIfNeed(childFragmentManager).state = state
        } else {
            onClose(state)
        }
    }

    private suspend fun processState() {
        viewModel.paymentProcessState.collect {
            handleLoadingInProcess(it is PaymentByCardState.Started)
            when (it) {
                is PaymentByCardState.Created -> statusSheetStatus.state = null
                is PaymentByCardState.Error -> {
                    handleFinalState(
                        PaymentStatusSheetState.Error(
                            title = R.string.acq_commonsheet_failed_title,
                            mainButton = R.string.acq_commonsheet_clear_primarybutton,
                            throwable = it.throwable
                        )
                    )
                }

                is PaymentByCardState.Started -> Unit
                is PaymentByCardState.Success -> {
                    handleFinalState(
                        PaymentStatusSheetState.Success(
                            title = R.string.acq_commonsheet_paid_title,
                            mainButton = R.string.acq_commonsheet_clear_primarybutton,
                            paymentId = it.paymentId,
                            cardId = it.cardId,
                            rebillId = it.rebillId
                        )
                    )
                }

                is PaymentByCardState.ThreeDsUiNeeded -> {
                    try {
                        ThreeDsHelper.Launch(
                            activity = requireActivity(),
                            options = it.paymentOptions,
                            threeDsData = it.threeDsState.data,
                            browserBasedLauncher = threeDsBrowserBasedLauncher
                        )
                    } catch (e: Throwable) {
                        statusSheetStatus.showIfNeed(childFragmentManager).state =
                            PaymentStatusSheetState.Error(
                                title = R.string.acq_commonsheet_failed_title,
                                mainButton = R.string.acq_commonsheet_clear_primarybutton,
                                throwable = e
                            )
                    } finally {
                        viewModel.goTo3ds()
                    }
                }

                is PaymentByCardState.ThreeDsInProcess -> {
                    handleLoadingInProcess(true)
                }

                else -> Unit
            }
        }
    }

    private suspend fun selectedCardState() {
        viewModel.state.collectLatest {
            savedCardOptions.features.apply { selectedCardId = it.chosenCard?.id }
        }
    }
    //endregion

    private fun handleLoadingInProcess(inProcess: Boolean) {
        if(inProcess) {
            requireActivity().window.setFlagNotTouchable()
        } else {
            requireActivity().window.setFlagTouchable()
        }
        payButton.isLoading = inProcess
        payButton.isClickable = !inProcess
        emailInput.viewModel.enableEmail(inProcess.not())
        chosenCardComponent.enableCvc(inProcess.not())
        onBackEnabled = inProcess.not()
    }

    private fun finishWithSuccess(result: PaymentResult) {
        requireActivity().setResult(
            AppCompatActivity.RESULT_OK,
            PaymentByCardLauncher.Contract.createSuccessIntent(result)
        )
        requireActivity().finish()
    }
}

