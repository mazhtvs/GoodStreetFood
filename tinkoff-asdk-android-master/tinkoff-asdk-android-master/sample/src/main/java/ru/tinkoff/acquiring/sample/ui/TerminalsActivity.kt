package ru.tinkoff.acquiring.sample.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sample.databinding.ActivityTerminalsBinding
import ru.tinkoff.acquiring.sample.databinding.ItemTerminalBinding
import ru.tinkoff.acquiring.sample.utils.SessionParams
import ru.tinkoff.acquiring.sample.utils.TerminalsManager

class TerminalsActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityTerminalsBinding
    private lateinit var terminals: MutableList<SessionParams>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityTerminalsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        updateList()
        viewBinding.recycler.adapter = Adapter()

        viewBinding.add.setOnClickListener {
            openEditFragment(null)
        }

        viewBinding.reset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset all terminal settings to default?")
                .setPositiveButton("Yes") { _, _ ->
                    TerminalsManager.reset()
                    updateList()
                }.setNegativeButton("No") { _, _ -> }
                .show()
        }
    }

    private fun openEditFragment(terminalKey: String?) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EditTerminalFragment.newInstance(terminalKey))
            .addToBackStack(null)
            .commit()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList() {
        terminals = TerminalsManager.terminals.toMutableList()
        viewBinding.recycler.adapter?.notifyDataSetChanged()
    }

    inner class Adapter : RecyclerView.Adapter<VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_terminal, parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(terminals.get(position))
        }

        override fun getItemCount(): Int = terminals.size
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {

        private val viewBinding = ItemTerminalBinding.bind(view)

        fun bind(sessionParams: SessionParams) = with(itemView) {
            viewBinding.terminalKey.text = sessionParams.terminalKey

            viewBinding.description.text = sessionParams.description
            viewBinding.description.isGone = sessionParams.description.isNullOrEmpty()

            viewBinding.publicKey.text = sessionParams.publicKey
            viewBinding.password.text = sessionParams.password.takeUnless { it.isNullOrBlank() } ?: "<blank>"

            viewBinding.customerKey.text = sessionParams.customerKey
            viewBinding.customerEmail.text = sessionParams.customerEmail


            viewBinding.selectedIcon.isGone = sessionParams.terminalKey != TerminalsManager.selectedTerminalKey

            viewBinding.delete.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete terminal?")
                    .setPositiveButton("Yes") { _, _ ->
                        terminals.remove(sessionParams)
                        TerminalsManager.terminals = terminals
                        updateList()
                    }.setNegativeButton("No") { _, _ -> }
                    .show()
            }

            viewBinding.edit.setOnClickListener {
                openEditFragment(sessionParams.terminalKey)
            }

            setOnClickListener {
                TerminalsManager.selectedTerminalKey = sessionParams.terminalKey
                updateList()
            }
        }
    }
}
