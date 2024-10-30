package ru.tinkoff.acquiring.sample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.tinkoff.acquiring.sample.databinding.FragmentEditTerminalBinding
import ru.tinkoff.acquiring.sample.utils.SessionParams
import ru.tinkoff.acquiring.sample.utils.TerminalsManager
import ru.tinkoff.acquiring.sdk.utils.Base64
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

class EditTerminalFragment : Fragment() {

    private var viewBinding: FragmentEditTerminalBinding? = null

    var oldTerminalKey: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentEditTerminalBinding.inflate(inflater, container, false)
            .also { viewBinding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = viewBinding!!

        oldTerminalKey = arguments?.getString(TERMINAL_KEY)

        oldTerminalKey?.let { TerminalsManager.requireTerminal(it) }?.run {
            binding.terminalKeyInput.setText(terminalKey)
            binding.descriptionInput.setText(description)
            binding.publicKeyInput.setText(publicKey)
            binding.passwordInput.setText(password)
            binding.customerKeyInput.setText(customerKey)
            binding.customerEmailInput.setText(customerEmail)
        }

        binding.save.setOnClickListener {
            val terminal = SessionParams(
                binding.terminalKeyInput.text.toString().trim(),
                binding.passwordInput.text.toString().trim().takeUnless { it.isBlank() },
                binding.publicKeyInput.text.toString().trim(),
                binding.customerKeyInput.text.toString().trim(),
                binding.customerEmailInput.text.toString().trim(),
                binding.descriptionInput.text.toString().trim().takeUnless { it.isBlank() },
            )

            try {
                terminal.validate()
            } catch (e: Throwable) {
                toast(e.message!!)
                return@setOnClickListener
            }

            saveTerminal(terminal)
            close()
        }

        binding.cancel.setOnClickListener {
            close()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    fun close() {
        (requireActivity() as TerminalsActivity).updateList()
        requireActivity().onBackPressed()
    }

    fun saveTerminal(terminal: SessionParams) {
        val oldTerminalKey = oldTerminalKey
        val oldSelectedTerminalKey = TerminalsManager.selectedTerminalKey
        val terminals = TerminalsManager.terminals.toMutableList()
        if (oldTerminalKey != null) {
            val oldTerminal = terminals.first { it.terminalKey == oldTerminalKey }
            val oldIndex = terminals.indexOf(oldTerminal)
            terminals.remove(oldTerminal)
            terminals.add(oldIndex, terminal)
        } else {
            terminals.add(terminal)
        }
        TerminalsManager.terminals = terminals

        if (oldSelectedTerminalKey == oldTerminalKey) {
            TerminalsManager.selectedTerminalKey = terminal.terminalKey
        }
    }

    private fun SessionParams.validate() {
        if (oldTerminalKey != terminalKey) {
            check(TerminalsManager.findTerminal(terminalKey) == null) {
                "Terminal with this key already exists"
            }
        }
        check(terminalKey.isNotBlank()) { "Terminal Key can't be empty" }
        check(publicKey.isNotBlank()) { "Public Key can't be empty" }
        check(validatePublicKey(publicKey)) { "Error parsing public key" }
        check(customerKey.isNotBlank()) { "Customer Key can't be empty" }
    }

    private fun validatePublicKey(source: String): Boolean {
        return try {
            val publicBytes = Base64.decode(source, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(publicBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(keySpec)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {

        private const val TERMINAL_KEY = "terminal_key"

        fun newInstance(terminalKey: String?): EditTerminalFragment {
            val args = Bundle()
            args.putString(TERMINAL_KEY, terminalKey)
            val fragment = EditTerminalFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
