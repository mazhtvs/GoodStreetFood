package ru.tinkoff.acquiring.sdk.redesign.mainform.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.databinding.AcqCardPayComponentBinding
import ru.tinkoff.acquiring.sdk.databinding.AcqMainFormPrimaryButtonComponentBinding
import ru.tinkoff.acquiring.sdk.databinding.AcqMainFormSecondaryBlockBinding
import ru.tinkoff.acquiring.sdk.databinding.AcqMainFromActivityBinding
import ru.tinkoff.acquiring.sdk.databinding.AcqMainFromErrorStubBinding
import ru.tinkoff.acquiring.sdk.databinding.AcqPaymentStatusFormBinding
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.models.result.PaymentResult
import ru.tinkoff.acquiring.sdk.redesign.cards.list.ChooseCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.common.LauncherConstants.RESULT_ERROR
import ru.tinkoff.acquiring.sdk.redesign.common.cardpay.CardPayComponent
import ru.tinkoff.acquiring.sdk.redesign.common.result.AcqPaymentResult
import ru.tinkoff.acquiring.sdk.redesign.common.util.AcqShimmerAnimator
import ru.tinkoff.acquiring.sdk.redesign.dialog.PaymentStatusSheetState
import ru.tinkoff.acquiring.sdk.redesign.dialog.component.PaymentStatusComponent
import ru.tinkoff.acquiring.sdk.redesign.mainform.MainFormLauncher
import ru.tinkoff.acquiring.sdk.redesign.mainform.navigation.MainFormNavController
import ru.tinkoff.acquiring.sdk.redesign.mainform.presentation.vm.MainFormInputCardViewModel
import ru.tinkoff.acquiring.sdk.redesign.mainform.presentation.vm.MainPaymentFormViewModel
import ru.tinkoff.acquiring.sdk.redesign.mainform.presentation.vm.MainPaymentFormViewModelFactory
import ru.tinkoff.acquiring.sdk.redesign.mirpay.MirPayLauncher
import ru.tinkoff.acquiring.sdk.redesign.payment.PaymentByCardLauncher
import ru.tinkoff.acquiring.sdk.redesign.sbp.SbpPayLauncher
import ru.tinkoff.acquiring.sdk.redesign.tpay.TpayLauncher
import ru.tinkoff.acquiring.sdk.smartfield.AcqTextFieldView
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper
import ru.tinkoff.acquiring.sdk.ui.activities.ThreeDsLauncher
import ru.tinkoff.acquiring.sdk.ui.customview.editcard.keyboard.SecureKeyboardController
import ru.tinkoff.acquiring.sdk.utils.applyThemeMode
import ru.tinkoff.acquiring.sdk.utils.getOptions
import ru.tinkoff.acquiring.sdk.utils.lazyUnsafe
import ru.tinkoff.acquiring.sdk.utils.lazyView
import ru.tinkoff.acquiring.sdk.utils.putOptions
import ru.tinkoff.acquiring.sdk.utils.setFlagNotTouchable
import ru.tinkoff.acquiring.sdk.utils.setFlagTouchable
import ru.tinkoff.acquiring.sdk.utils.setupCvcInput


/**
 * Created by i.golovachev
 */
internal class MainPaymentFormActivity : AppCompatActivity() {

    val options by lazyUnsafe { intent.getOptions<PaymentOptions>() }

    private lateinit var binding: AcqMainFromActivityBinding

    private val byNewCardPayment = registerForActivityResult(PaymentByCardLauncher.Contract) {
        when (it) {
            is PaymentByCardLauncher.Success -> onPayByNewCardSuccess(it)
            is PaymentByCardLauncher.Error -> onPayByNewCardError(it)
            is PaymentByCardLauncher.Canceled -> onPayByNewCardCanceled()
        }
    }

    private val spbPayment = registerForActivityResult(SbpPayLauncher.Contract) {
        when (it) {
            is SbpPayLauncher.Success -> onSbpPaySuccess(it)
            is SbpPayLauncher.Error -> onSbpPaymentError(it)
            is SbpPayLauncher.Canceled -> Unit
            is SbpPayLauncher.NoBanks -> Unit
        }
    }

    private val tpayPayment = registerForActivityResult(TpayLauncher.Contract) {
        when (it) {
            is TpayLauncher.Success -> onTpaySuccess(it)
            is TpayLauncher.Error -> onTpayError(it)
            is TpayLauncher.Canceled -> onTpayCancelled()
        }
    }

    private val mirPayPayment = registerForActivityResult(MirPayLauncher.Contract) {
        when (it) {
            is MirPayLauncher.Success -> onMirPaySuccess(it)
            is MirPayLauncher.Error -> onMirPayError(it)
            is MirPayLauncher.Canceled -> onMirpayCancelled()
        }
    }

    private val savedCards = registerForActivityResult(ChooseCardLauncher.Contract) {
        when (it) {
            is ChooseCardLauncher.CardSelected -> onCardSelected(it)
            is ChooseCardLauncher.PaidByNewCard -> onPaidByNewCard(it)
            is ChooseCardLauncher.Error -> onPayByNewCardError(it)
            is ChooseCardLauncher.Canceled -> onChooseCardCanceled()
        }
    }

    private val browserBasedThreeDsLauncher = registerForActivityResult(ThreeDsLauncher.Contract) {
        when (it) {
            is ThreeDsLauncher.Result.Success -> on3DsSuccess(it)
            is ThreeDsLauncher.Result.Error -> on3DsError(it)
            ThreeDsLauncher.Result.Cancelled -> Unit
        }
    }

    private val factory by lazyUnsafe { MainPaymentFormViewModelFactory(application, options) }
    private val viewModel: MainPaymentFormViewModel by viewModels { factory }
    private val cardInputViewModel: MainFormInputCardViewModel by viewModels { factory }

    private val root: CoordinatorLayout by lazyView(R.id.acq_main_form_root)
    private val shimmer: ViewGroup by lazyView(R.id.acq_main_form_loader)
    private val content: LinearLayout by lazyView(R.id.acq_main_form_content)
    private val amount: TextView by lazyView(R.id.acq_main_form_amount)
    private val description: TextView by lazyView(R.id.acq_main_form_description)
    private val sheet: NestedScrollView by lazyView(R.id.acq_main_form_sheet)

    private val bottomSheetComponent by lazyUnsafe {
        BottomSheetComponent(root, sheet) {
            viewModel.onBackPressed()
        }
    }

    private val primaryButtonComponent by lazyUnsafe {
        PrimaryButtonComponent(
                viewBinding = AcqMainFormPrimaryButtonComponentBinding.bind(
                        findViewById(R.id.acq_main_form_primary_button)
                ),
                onMirPayClick = viewModel::toMirPay,
                onNewCardClick = viewModel::toNewCard,
                onSpbClick = viewModel::toSbp,
                onTpayClick = viewModel::toTpay,
                onPayClick = cardInputViewModel::pay
        )
    }

    private val cardPayComponent by lazyUnsafe {
        CardPayComponent(
                this.root,
                viewBinding = AcqCardPayComponentBinding.bind(
                        findViewById(R.id.acq_main_card_pay)
                ),
                email = options.customer.email,
                onCvcCompleted = cardInputViewModel::setCvc,
                onEmailInput = cardInputViewModel::email,
                onEmailVisibleChange = {
                    lifecycleScope.launch {
                        bottomSheetComponent.trimSheetToContent(content)
                    }
                    cardInputViewModel.needEmail(it)
                },
                onChooseCardClick = viewModel::toChooseCard,
                onPayClick = { cardInputViewModel.pay() },
                useSecureKeyboard = options.features.useSecureKeyboard
        )
    }

    private val secondaryButtonComponent by lazyUnsafe {
        SecondaryBlockComponent(
                binding = AcqMainFormSecondaryBlockBinding.bind(
                        findViewById(R.id.acq_main_form_secondary_button)
                ),
                onNewCardClick = viewModel::toPayCardOrNewCard,
                onSpbClick = viewModel::toSbp,
                onTpayClick = { viewModel.toTpay(false) },
                onMirPayClick = viewModel::toMirPay,
        )
    }

    private val paymentStatusComponent by lazyUnsafe {
        PaymentStatusComponent(
                viewBinding = AcqPaymentStatusFormBinding.bind(findViewById(R.id.acq_payment_status)),
                onMainButtonClick = { viewModel.onBackPressed() },
                onSecondButtonClick = { viewModel.onBackPressed() },
        )
    }

    private val errorStubComponent by lazyUnsafe {
        ErrorStubComponent(
                viewBinding = AcqMainFromErrorStubBinding.bind(findViewById(R.id.acq_main_from_error_stub)),
                onRetry = viewModel::onRetry
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = AcqMainFromActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCvcClickListener(root, cardPayComponent)
        initTheme()
        createTitleView()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        lifecycleScope.launch { updateContent() }
        lifecycleScope.launch { updatePayEnable() }
        lifecycleScope.launch { updateButtonLoader() }
        lifecycleScope.launch { updatePrimary() }
        lifecycleScope.launch { updateSecondary() }
        lifecycleScope.launch { updateSavedCard() }
        lifecycleScope.launch { updateCardPayState() }
        lifecycleScope.launch {
            cardInputViewModel.savedCardFlow.collectLatest {
                cardPayComponent.renderNewCard(it)
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                subscribeOnNav()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val secureKeyboardController = SecureKeyboardController.getInstance()
        secureKeyboardController.registerInsets(binding.root)
        secureKeyboardController.setKeyboardStateListener(object: SecureKeyboardController.KeyboardStateListener {
            override fun onPaddingUpdated(height: Int, navigationHeight: Int): Boolean {
                binding.acqMainFormFlipper.updatePadding(bottom = maxOf(height, navigationHeight))
                return true
            }
        })
    }

    override fun onPause() {
        super.onPause()
        SecureKeyboardController.getInstance().clear()
    }

    override fun onAttachedToWindow() {
        bottomSheetComponent.onAttachedToWindow()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    private fun initCvcClickListener(root: ViewGroup, card: CardPayComponent) {
        root.findViewById<AcqTextFieldView>(R.id.cvc_input).setupCvcInput(root, card)
    }

    private suspend fun updateContent() {
        combine(
            cardInputViewModel.paymentStatus, viewModel.formContent
        ) { cardStatus, formContent ->
            if (cardStatus is PaymentStatusSheetState.Error ||
                cardStatus is PaymentStatusSheetState.Success
            ) {
                if (options.features.showPaymentNotifications) {
                    shimmer.isVisible = false
                    content.isVisible = false
                    errorStubComponent.isVisible(false)
                    paymentStatusComponent.isVisible = true
                    paymentStatusComponent.render(cardStatus)
                    bottomSheetComponent.trimSheetToContent(paymentStatusComponent.viewBinding.root)
                    bottomSheetComponent.collapse()
                } else {
                    bottomSheetComponent.collapse()
                    viewModel.returnResult()
                }
            } else {
                paymentStatusComponent.isVisible = false
                when (formContent) {
                    is MainPaymentFormViewModel.FormContent.Loading -> {
                        errorStubComponent.isVisible(false)
                        shimmer.isVisible = true
                        content.isVisible = false
                        bottomSheetComponent.trimSheetToContent(shimmer)
                        AcqShimmerAnimator.animateSequentially(shimmer.children.toList())
                    }
                    is MainPaymentFormViewModel.FormContent.Error -> {
                        shimmer.isVisible = false
                        content.isVisible = false
                        errorStubComponent.isVisible(true)
                        errorStubComponent.render(ErrorStubComponent.State.Error)
                        bottomSheetComponent.trimSheetToContent(errorStubComponent.root)
                        bottomSheetComponent.collapse()
                    }
                    is MainPaymentFormViewModel.FormContent.Content -> {
                        shimmer.isVisible = false
                        content.isVisible = true
                        errorStubComponent.isVisible(false)
                        cardPayComponent.isVisible(formContent.isSavedCard)
                        primaryButtonComponent.isVisible(formContent.isSavedCard.not())
                        bottomSheetComponent.trimSheetToContent(content)
                        bottomSheetComponent.collapse()
                        description.text = formContent.description
                        description.isVisible = formContent.showDescription
                    }
                    is MainPaymentFormViewModel.FormContent.NoNetwork -> {
                        shimmer.isVisible = false
                        content.isVisible = false
                        errorStubComponent.isVisible(true)
                        errorStubComponent.render(ErrorStubComponent.State.NoNetwork)
                        bottomSheetComponent.trimSheetToContent(errorStubComponent.root)
                        bottomSheetComponent.collapse()
                    }
                    is MainPaymentFormViewModel.FormContent.Hide -> {
                        shimmer.isVisible = false
                        content.isVisible = false
                        errorStubComponent.isVisible(false)
                        bottomSheetComponent.trimSheetToContent(paymentStatusComponent.viewBinding.root)
                        bottomSheetComponent.collapse()
                    }
                }
            }
        }.collect()
    }

    private suspend fun updatePrimary() = viewModel.primary.collect {
        primaryButtonComponent.render(it)
    }

    private suspend fun updateSecondary() = viewModel.secondary.collect {
        secondaryButtonComponent.render(it)
    }

    private suspend fun updateSavedCard() = viewModel.chosenCard.collect {
        cardInputViewModel.choseCard(it)
    }

    private suspend fun updatePayEnable() = cardInputViewModel.payEnable.collectLatest {
        cardPayComponent.renderEnable(it)
    }

    private suspend fun updateButtonLoader() = cardInputViewModel.isLoading.collectLatest {
        cardPayComponent.renderLoader(it)
        if (it) {
            window.setFlagNotTouchable()
            cardPayComponent.isKeyboardVisible(false)
        } else {
            window.setFlagTouchable()
        }
        handleLoadingInProcess(it)
    }

    private suspend fun updateCardPayState() = with(cardInputViewModel) {
        combine(savedCardFlow, emailFlow) { card, email -> card to email }
            .take(1)
            .collectLatest { (card, email) ->
                cardPayComponent.render(card, email, options)
            }
    }

    private suspend fun subscribeOnNav() {
        viewModel.mainFormNav.collect {
            when (it) {
                is MainFormNavController.Navigation.ToChooseCard -> {
                    cardPayComponent.isKeyboardVisible(false)
                    savedCards.launch(it.startData)
                }
                is MainFormNavController.Navigation.ToPayByCard -> {
                    byNewCardPayment.launch(it.startData)
                }
                is MainFormNavController.Navigation.ToSbp -> {
                    spbPayment.launch(it.startData)
                }
                is MainFormNavController.Navigation.ToTpay -> {
                    tpayPayment.launch(it.startData)
                }
                is MainFormNavController.Navigation.ToMirPay -> {
                    mirPayPayment.launch(it.startData)
                }
                is MainFormNavController.Navigation.To3ds -> {
                    ThreeDsHelper.Launch(
                        activity = this,
                        options = it.paymentOptions,
                        threeDsData = it.threeDsState.data,
                        browserBasedLauncher = browserBasedThreeDsLauncher
                    )
                }
                is MainFormNavController.Navigation.Return -> {
                    when (it.result) {
                        is AcqPaymentResult.Canceled -> finishCancelled()
                        is AcqPaymentResult.Error -> {
                            finishError(MainFormLauncher.Contract.createFailedIntent(it.result.error))
                        }
                        is AcqPaymentResult.Success -> {
                            finishSuccess(MainFormLauncher.Contract.createSuccessIntent(it.result))
                        }
                    }
                }
                is MainFormNavController.Navigation.ToWebView -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))
                }
                null -> Unit
            }
        }
    }

    private fun createTitleView() {
        amount.text = options.order.amount.toHumanReadableString()
    }

    private fun handleLoadingInProcess(inProcess: Boolean) {
        cardPayComponent.isEnable(inProcess.not())
    }

    private fun initTheme() {
        applyThemeMode(options.features.darkThemeMode)
    }

    private fun onTpaySuccess(it: TpayLauncher.Success) {
        finishSuccess(MainFormLauncher.Contract.createSuccessIntent(it))
    }

    private fun onTpayCancelled() {
        finishCancelled()
    }

    private fun onTpayError(error: TpayLauncher.Error) {
        returnToPaymentFormOrFinishWithError(error.error)
    }

    private fun onMirPaySuccess(it: MirPayLauncher.Success) {
        finishSuccess(MainFormLauncher.Contract.createSuccessIntent(it))
    }

    private fun onMirpayCancelled() {
        finishCancelled()
    }

    private fun onMirPayError(error: MirPayLauncher.Error) {
        returnToPaymentFormOrFinishWithError(error.error)
    }

    private fun onSbpPaySuccess(it: SbpPayLauncher.Success) {
        finishSuccess(MainFormLauncher.Contract.createSuccessIntent(it.payment))
    }

    private fun onSbpPaymentError(it: SbpPayLauncher.Error) {
        returnToPaymentFormOrFinishWithError(it.error)
    }

    private fun returnToPaymentFormOrFinishWithError(e: Throwable) {
        if (!options.features.showPaymentNotifications) {
            finishError(MainFormLauncher.Contract.createFailedIntent(e))
        } else {
            viewModel.returnOnForm()
        }
    }

    private fun onChooseCardCanceled() {
        viewModel.loadState()
    }

    private fun onPayByNewCardCanceled() {
        viewModel.loadState()
    }

    private fun onPayByNewCardError(it: ChooseCardLauncher.Error) {
        finishError(MainFormLauncher.Contract.createFailedIntent(it.error))
    }

    private fun onPayByNewCardError(it: PaymentByCardLauncher.Error) {
        finishError(MainFormLauncher.Contract.createFailedIntent(it))
    }

    private fun onPayByNewCardSuccess(it: PaymentByCardLauncher.Success) {
        finishSuccess(MainFormLauncher.Contract.createSuccessIntent(it))
    }

    private fun onCardSelected(it: ChooseCardLauncher.CardSelected) {
        viewModel.choseCard(it.card)
        cardInputViewModel.choseCard(it.card)
    }

    private fun onPaidByNewCard(it: ChooseCardLauncher.PaidByNewCard) {
        finishSuccess(MainFormLauncher.Contract.createSuccessIntent(it))
    }

    private fun finishSuccess(intent: Intent) {
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun finishError(intent: Intent) {
        setResult(RESULT_ERROR, intent)
        finish()
    }

    private fun finishCancelled() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun on3DsError(it: ThreeDsLauncher.Result.Error) {
        cardInputViewModel.set3dsResult(it)
    }

    private fun on3DsSuccess(it: ThreeDsLauncher.Result.Success) {
        cardInputViewModel.set3dsResult(it.result as PaymentResult)
    }

    internal companion object {
        fun intent(
            context: Context,
            options: PaymentOptions,
        ): Intent {
            val intent = Intent(context, MainPaymentFormActivity::class.java)
            intent.putOptions(options)
            return intent
        }
    }
}
