package ru.tinkoff.acquiring.sample.ui.environment


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sdk.AcquiringSdk

/**
 * Created by i.golovachev
 */
class AcqEnvironmentDialog : DialogFragment() {

    companion object {
        private const val HTTPS_PREFIX = "https://"
        private const val PRE_PROD_URL = "https://qa-mapi.tcsbank.ru"
        private const val DEBUG_URL = "https://rest-api-test.tinkoff.ru"
        const val TAG = "AcqEnvironmentDialog"
    }

    private val ok: TextView by lazy {
        requireView().findViewById(R.id.acq_env_ok)
    }
    private val editUrlText: EditText by lazy {
        requireView().findViewById(R.id.acq_env_url)
    }
    private val evnGroup: RadioGroup by lazy {
        requireView().findViewById(R.id.acq_env_urls)
    }
    private var customUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customUrl = context?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getString("customUrl", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                ?.edit()
                ?.putString("customUrl", customUrl)
                ?.apply()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.asdk_environment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        evnGroup.setOnCheckedChangeListener { _, checkedId ->
            onRadioGroupChecked(checkedId)
        }

        setupEnv()

        ok.setOnClickListener {
            onOkButtonClick()
        }
    }

    override fun onResume() {
        dialog?.window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        super.onResume()
    }

    private fun onRadioGroupChecked(checkedId: Int) {
        when (checkedId) {
            R.id.acq_env_is_pre_prod_btn -> fillReadOnlyUrl(PRE_PROD_URL)
            R.id.acq_env_is_debug_btn -> fillReadOnlyUrl(DEBUG_URL)
            R.id.acq_env_is_custom_btn -> prefillCustomUrlTemplate()
        }
    }

    private fun prefillCustomUrlTemplate() {
        with(editUrlText) {
            setText(HTTPS_PREFIX)
            setSelection(text.length)
            isEnabled = true
            requestFocus()
        }
    }

    private fun onOkButtonClick() {
        saveSettings()
        dismiss()
    }

    private fun saveSettings() {
        val url = when (evnGroup.checkedRadioButtonId) {
            R.id.acq_env_is_custom_btn -> {
                editUrlText.text.toString()
                        .also { customUrl = it }
            }

            R.id.acq_env_is_pre_prod_btn -> {
                PRE_PROD_URL
            }

            else -> {
                null
            }
        }
        AcquiringSdk.customUrl = url
    }

    private fun fillReadOnlyUrl(url: String) {
        with(editUrlText) {
            setText(url)
            isEnabled = false
        }
    }

    private fun setupEnv() {
        val radioGroupId = when (AcquiringSdk.customUrl) {
            PRE_PROD_URL -> R.id.acq_env_is_pre_prod_btn
            null -> R.id.acq_env_is_debug_btn
            else -> R.id.acq_env_is_custom_btn
        }
        evnGroup.check(radioGroupId)
    }
}
