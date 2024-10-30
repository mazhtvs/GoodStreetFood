package ru.tinkoff.acquiring.sample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.tinkoff.acquiring.sample.SampleApplication
import ru.tinkoff.acquiring.sample.databinding.DialogAttachCardManuallyBinding
import ru.tinkoff.acquiring.sample.utils.SessionParams
import ru.tinkoff.acquiring.sample.utils.TerminalsManager
import ru.tinkoff.acquiring.sample.utils.toast
import ru.tinkoff.acquiring.sdk.models.enums.ResponseStatus
import ru.tinkoff.acquiring.sdk.models.options.screen.BaseAcquiringOptions
import ru.tinkoff.acquiring.sdk.models.paysources.CardData
import ru.tinkoff.acquiring.sdk.threeds.ThreeDsHelper

class AttachCardManuallyDialogFragment : DialogFragment() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val sdk = SampleApplication.tinkoffAcquiring.sdk

    private var viewBinding:DialogAttachCardManuallyBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DialogAttachCardManuallyBinding.inflate(inflater, container, false)
            .also { viewBinding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding!!) {
            close.setOnClickListener { dismiss() }

            attach.setOnClickListener {
                val cardData =
                    CardData(pan.text.toString(), date.text.toString(), cvc.text.toString())
                try {
                    cardData.validate()
                } catch (e: Throwable) {
                    requireActivity().toast(e.message!!)
                    return@setOnClickListener
                }
                addCardManually(cardData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    private fun addCardManually(cardData: CardData) {
        val params = TerminalsManager.selectedTerminal

        val addCard = sdk.addCard {
            customerKey = params.customerKey
            checkType = "3DS"
        }

        coroutineScope.launch(Dispatchers.IO) {
            addCard.execute({
                attachCardManually(params, it.requestKey!!, cardData)
            }, { })
        }
    }

    private fun attachCardManually(params: SessionParams, requestKey: String, cardData: CardData) {
        val attachCard = sdk.attachCard {
            this.requestKey = requestKey
            this.cardData = cardData
        }
        val options = BaseAcquiringOptions().apply {
            setTerminalParams(params.terminalKey, params.publicKey)
        }

        coroutineScope.launch(Dispatchers.IO) {
            attachCard.execute({
                when (it.status) {
                    ResponseStatus.THREE_DS_CHECKING ->
                        coroutineScope.launch {
                            val mainActivity = requireActivity() as MainActivity
                            ThreeDsHelper.Launch(
                                activity = mainActivity,
                                options = options,
                                threeDsData = it.getThreeDsData(),
                                browserBasedLauncher = mainActivity.threeDsBrowserBasedLauncher
                            )
                        }
                    null -> {
                        requireActivity().toast("Attach success")
                        dismiss()
                    }
                    else -> requireActivity().toast("Attach failure: ${it.status}")
                }
            }, { requireActivity().toast("Attach failure: ${it.message}") })
        }
    }

    companion object {

        const val TAG = "AttachCardManuallyDialogFragment"
    }
}
