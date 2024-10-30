package ru.tinkoff.acquiring.sdk.redesign.payment.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.redesign.common.carddatainput.CvcComponentFocusDecorator
import ru.tinkoff.acquiring.sdk.redesign.payment.model.CardChosenModel
import ru.tinkoff.acquiring.sdk.ui.component.UiComponent
import ru.tinkoff.acquiring.sdk.utils.KeyboardVisionUtils.hideKeyboard
import ru.tinkoff.acquiring.sdk.utils.KeyboardVisionUtils.showKeyboard
import ru.tinkoff.acquiring.sdk.viewmodel.CardLogoProvider


/**
 * Created by i.golovachev
 */
internal class ChosenCardComponent(
    private val root: ViewGroup,
    private val initingFocusAndKeyboard: Boolean = false,
    private val onChangeCard: (CardChosenModel) -> Unit = {},
    private val onCvcCompleted: (String, Boolean) -> Unit = { _, _ -> },
    private val onFocusCvc: View.() -> Unit = {
        requestFocus()
        isEnabled = true
    },
    useSecureKeyboard: Boolean
) : UiComponent<CardChosenModel> {

    private val cardLogo: ImageView = root.findViewById(R.id.acq_card_choosen_item_logo)
    private val cardName: TextView = root.findViewById(R.id.acq_card_choosen_item)
    private val cardChange: TextView = root.findViewById(R.id.acq_card_change)
    private val cardCvc: CvcComponentFocusDecorator = CvcComponentFocusDecorator(
        root.findViewById(R.id.cvc_container),
        initingFocusAndKeyboard,
        onFocusCvc,
        onInputComplete = { s ->
            onCvcCompleted(s, true)
        },
        onDataChange = { b, s ->
            onCvcCompleted(s, b)
        },
        useSecureKeyboard = useSecureKeyboard,
    )

    override fun render(state: CardChosenModel) = with(state) {
        cardLogo.setImageResource(CardLogoProvider.getCardLogo(pan))
        cardName.text = root.context.getString(
            R.string.acq_cardlist_bankname, bankName.orEmpty(), tail
        )
        root.setOnClickListener { onChangeCard(state) }
    }

    fun renderCvcOnly(state: CardChosenModel)  = with(state) {
        cardLogo.setImageResource(CardLogoProvider.getCardLogo(pan))
        cardName.text = root.context.getString(
            R.string.acq_cardlist_bankname, bankName.orEmpty(), tail
        )
        cardChange.isVisible = false
        root.setOnClickListener {  }
    }

    fun clearCvc() {
        cardCvc.render(null)
    }

    fun clearInput(){
        cardCvc.clearInput(null)
    }

    fun enableCvc(isEnable: Boolean) {
        cardCvc.enable(isEnable)
    }

    fun enableKeyboard(){
        showKeyboard(root)
    }

    fun disableKeyboard(){
        hideKeyboard(root)
    }
}
