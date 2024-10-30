package ru.tinkoff.acquiring.sample.ui.toggles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import ru.tinkoff.acquiring.sample.R
import ru.tinkoff.acquiring.sdk.toggles.FeatureToggleManager
import ru.tinkoff.acquiring.sdk.toggles.Toggles

/**
 * @author k.shpakovskiy
 */
class TogglesAdapter(
    private val manager: FeatureToggleManager
) : RecyclerView.Adapter<TogglesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_toggle, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int { return Toggles.list.size }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(Toggles.list[position]) {
            holder.name.text = name
            holder.switcher.isChecked = manager.isEnabled(this)
            holder.switcher.setOnCheckedChangeListener { buttonView, isChecked ->
                manager.changeValue(this@with, isChecked )
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView
        val switcher: SwitchCompat

        init {
            name = view.findViewById(R.id.toggle_name)
            switcher = view.findViewById(R.id.toggle_switch)
        }
    }
}
