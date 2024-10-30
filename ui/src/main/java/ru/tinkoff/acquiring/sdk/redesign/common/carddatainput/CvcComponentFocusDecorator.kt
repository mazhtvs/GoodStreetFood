package ru.tinkoff.acquiring.sdk.redesign.common.carddatainput

import android.view.View
import android.view.ViewGroup
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.smartfield.AcqTextFieldView
import ru.tinkoff.acquiring.sdk.ui.component.UiComponent

interface CvcUIComponent : UiComponent<String?> {
    fun clearCvc()
    fun enableCvc(isEnabled: Boolean)
    fun clearInput(text: String?)
}

class CvcComponentFocusDecorator(
    private val root: ViewGroup,
    private val initingFocusAndKeyboard: Boolean,
    private val onFocusCvc: View.() -> Unit,
    onInputComplete: (String) -> Unit,
    onDataChange: (Boolean, String) -> Unit,
    useSecureKeyboard: Boolean = false,
) : CvcUIComponent {

    val cvcInputView = root.findViewById<AcqTextFieldView>(R.id.cvc_input).apply {
        this.useSecureKeyboard = useSecureKeyboard
    }

    private val cvcComponent: CvcComponent = CvcComponent(
        root,
        initingFocusAndKeyboard,
        onInputComplete,
        onDataChange,
        onInitScreen = { _, function ->
            if(initingFocusAndKeyboard){
                onFocusCvc(cvcInputView.editText.apply(function))
            }
        }
    )

    override fun clearCvc() {
        cvcComponent.render(null)
    }

    override fun clearInput(text: String?) {
        cvcComponent.cvcInput.editText.setText("")
    }

    override fun enableCvc(isEnabled: Boolean) = cvcComponent.enable(isEnabled)

    fun enable(isEnable: Boolean) = with(cvcComponent.cvcInput) {
        isEnabled = isEnable
        editable = isEnable
        if (isEnable.not()) {
            hideKeyboard()
        }
    }

    fun focusCvc() {
        cvcComponent.cvcInput.requestViewFocus()
    }

    override fun render(state: String?) = cvcComponent.render(state)

}
