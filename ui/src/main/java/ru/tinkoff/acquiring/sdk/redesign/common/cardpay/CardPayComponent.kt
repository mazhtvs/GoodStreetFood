package ru.tinkoff.acquiring.sdk.redesign.common.cardpay

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.databinding.AcqCardPayComponentBinding
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.common.carddatainput.CvcComponentFocusDecorator
import ru.tinkoff.acquiring.sdk.redesign.common.emailinput.EmailInputComponent
import ru.tinkoff.acquiring.sdk.redesign.payment.model.CardChosenModel
import ru.tinkoff.acquiring.sdk.redesign.payment.ui.ChosenCardComponent
import ru.tinkoff.acquiring.sdk.ui.component.UiComponent
import ru.tinkoff.acquiring.sdk.ui.customview.LoaderButton
import ru.tinkoff.acquiring.sdk.utils.KeyboardVisionUtils

/**
 * Created by i.golovachev
 */
class CardPayComponent(
    private val root: ViewGroup,
    private val initingFocusAndKeyboard: Boolean = false,
    private val viewBinding: AcqCardPayComponentBinding,
    private val email: String?,
    private val onCvcCompleted: (String) -> Unit = {},
    private val onEmailInput: (String) -> Unit = {},
    private val onEmailVisibleChange: (Boolean) -> Unit = {},
    private val onChooseCardClick: () -> Unit = {},
    private val onPayClick: () -> Unit = {},
    private val onChangeCard: (CardChosenModel) -> Unit = {},
    private val onFocusCvc: View.() -> Unit = {
        requestFocus()
        isEnabled = true
    },
    useSecureKeyboard: Boolean = false,
): UiComponent<CardChosenModel> {

    private val loaderButton: LoaderButton = viewBinding.loaderButton.apply {
        setOnClickListener { onPayClick() }
    }

    private val cardCvc: CvcComponentFocusDecorator = CvcComponentFocusDecorator(
        root.findViewById(R.id.cvc_container),
        initingFocusAndKeyboard,
        onFocusCvc,
        onInputComplete = { s ->
            onCvcCompleted(s)
        },
        onDataChange = { b, s ->
            onCvcCompleted(s)
        },
        useSecureKeyboard = useSecureKeyboard
    )


    private val emailInputComponent = EmailInputComponent(viewBinding.emailInput.root,
        onEmailChange = { onEmailInput(it) },
        onEmailVisibleChange = { onEmailVisibleChange(it) }
    ).apply {
        render(EmailInputComponent.State(email, email != null))
    }

    private val savedCardComponent = ChosenCardComponent(viewBinding.chosenCard.root,
        onCvcCompleted = { cvc, _ -> onCvcCompleted(cvc) },
        onChangeCard = { onChooseCardClick() },
        onFocusCvc = { viewBinding.chosenCard.cvcContainer },
        useSecureKeyboard = useSecureKeyboard
    )

    fun render(state: CardChosenModel, email: String?, paymentOptions: PaymentOptions) {
        emailInputComponent.render(email, email.isNullOrBlank().not())
        savedCardComponent.render(state)
        loaderButton.text = viewBinding.root.resources.getString(
            R.string.acq_cardpay_pay,
            paymentOptions.order.amount.toHumanReadableString()
        )
    }

    fun renderNewCard(state:CardChosenModel) {
        savedCardComponent.render(state)
    }

    fun renderEnable(isEnable: Boolean) {
        loaderButton.isEnabled = isEnable
    }

    fun renderLoader(isLoading: Boolean) {
        loaderButton.isLoading = isLoading
        loaderButton.isClickable = !isLoading
    }

    fun renderInputCvc(card: CardChosenModel, paymentOptions: PaymentOptions) {
        viewBinding.emailInput.root.isVisible = false
        savedCardComponent.renderCvcOnly(card)
        savedCardComponent.enableCvc(true)
        loaderButton.text = viewBinding.root.resources.getString(
            R.string.acq_cardpay_pay,
            paymentOptions.order.amount.toHumanReadableString()
        )
    }

    fun isVisible(isVisible: Boolean) {
        viewBinding.root.isVisible = isVisible
    }

    fun isEnable(isEnable: Boolean) {
        savedCardComponent.enableCvc(isEnable)
        viewBinding.root.isEnabled = isEnable
        emailInputComponent.isEnable(isEnable)
    }

    fun isKeyboardVisible(isVisible: Boolean) {
        if (isVisible)
            KeyboardVisionUtils.showKeyboard(viewBinding.root)
        else
            KeyboardVisionUtils.hideKeyboard(viewBinding.root)
    }

    override fun render(state: CardChosenModel) {
        root.setOnClickListener { onChangeCard(state) }
    }

    fun clearCvc(){
        cardCvc.render(null)
    }

    fun focusCvc() {
        cardCvc.focusCvc()
    }
}
