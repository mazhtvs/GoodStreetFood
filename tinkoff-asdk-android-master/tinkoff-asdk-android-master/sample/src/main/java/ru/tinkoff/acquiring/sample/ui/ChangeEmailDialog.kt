package ru.tinkoff.acquiring.sample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import ru.tinkoff.acquiring.sample.databinding.DialogChangeEmailManuallyBinding
import ru.tinkoff.acquiring.sample.utils.SessionParams
import ru.tinkoff.acquiring.sample.utils.TerminalsManager

class ChangeEmailDialog : DialogFragment() {

    private var viewBinding: DialogChangeEmailManuallyBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DialogChangeEmailManuallyBinding.inflate(inflater, container, false)
            .also { viewBinding = it }
            .root
    }

    override fun onResume() {
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    private fun bindListeners() {
        with(viewBinding!!) {
            etMail.setText(SessionParams.TEST_SDK.customerEmail)
            tvClose.setOnClickListener { dismiss() }
            tvSave.setOnClickListener {
                SessionParams.changeEmail(etMail.text.toString())
                TerminalsManager.reset()
                dismiss()
            }
            tvDefault.setOnClickListener {
                SessionParams.toDefault()
                TerminalsManager.reset()
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "ChangeEmailDialog"
    }
}
