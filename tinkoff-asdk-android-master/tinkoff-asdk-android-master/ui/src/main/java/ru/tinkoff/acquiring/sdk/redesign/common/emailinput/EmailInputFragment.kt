package ru.tinkoff.acquiring.sdk.redesign.common.emailinput

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.lifecycleScope
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.smartfield.AcqTextFieldView
import ru.tinkoff.acquiring.sdk.smartfield.BaubleClearButton
import ru.tinkoff.acquiring.sdk.utils.getParent
import ru.tinkoff.acquiring.sdk.utils.lazyView

/**
 * Created by Michael Babayan
 */
class EmailInputFragment : Fragment() {
    private val emailInput: AcqTextFieldView by lazyView(R.id.email_input)
    val viewModel: EmailInputViewModel by viewModels {
        SavedStateViewModelFactory(requireActivity().application, this, arguments)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.acq_fragment_email_input, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BaubleClearButton().attach(emailInput)

        emailInput.editText.doAfterTextChanged { text ->
            val cursorPos = emailInput.editText.selectionStart
            viewModel.emailChanged(text?.toString(), cursorPos)
        }

        emailInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus.not()) {
                viewModel.checkValidation()
            }
        }

        with(lifecycleScope) {
            launchWhenResumed {
                handleEmailState()
            }
            launchWhenResumed {
                handleEmailChangeState()
            }
            launchWhenResumed {
                handleOneTimeEvents()
            }
        }
    }

    private suspend fun handleEmailChangeState() {
        viewModel.viewState.collect { state ->
            with(emailInput.editText) {
                isEnabled = state.emailEnabled
            }
        }
    }

    private suspend fun handleEmailState() {
        viewModel.combineEmailAndCursor().collect { (email, cursorPos, errorHighlighted) ->
            with(emailInput.editText) {
                setText(email)
                setSelection(cursorPos)
            }

            emailInput.errorHighlighted = errorHighlighted
            notifyDataChanged(emailInput.text.orEmpty(), !errorHighlighted)
        }
    }

    private suspend fun handleOneTimeEvents() {
        viewModel.oneTimeEvents.collect { state ->
            state.hideEmailInputKeyboard?.data?.takeIf { it }?.let {
                emailInput.hideKeyboard()
            }
            state.clearEmailInputFocus?.data?.takeIf { it }?.let {
                emailInput.clearViewFocus()
            }
            state.requestEmailInputFocus?.data?.takeIf { it }?.let {
                emailInput.requestFocus()
            }
        }
    }

    fun withArguments(email: String?): EmailInputFragment = apply {
        arguments = bundleOf(EMAIL_ARG to email)
    }

    private fun notifyDataChanged(email: String, isValid: Boolean) {
        getParent<OnEmailDataChanged>()?.onEmailDataChanged(email, isValid)
    }

    fun interface OnEmailDataChanged {
        fun onEmailDataChanged(email: String, isValid: Boolean)
    }

    companion object {
        const val EMAIL_ARG = "EMAIL_ARG"
        fun getInstance(email: String?) = EmailInputFragment().withArguments(email)
    }
}