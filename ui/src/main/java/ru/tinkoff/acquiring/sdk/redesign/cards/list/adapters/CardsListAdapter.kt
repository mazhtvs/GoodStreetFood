package ru.tinkoff.acquiring.sdk.redesign.cards.list.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.tinkoff.acquiring.sdk.R
import ru.tinkoff.acquiring.sdk.redesign.cards.list.adapters.CardsListAdapter.Companion.PAYLOAD_CHANGE_MODE
import ru.tinkoff.acquiring.sdk.redesign.cards.list.models.CardItemUiModel
import ru.tinkoff.acquiring.sdk.viewmodel.CardLogoProvider

/**
 * Created by Ivan Golovachev
 */
class CardsListAdapter(
    private val onDeleteClick: (CardItemUiModel) -> Unit,
    private val onChooseClick: (CardItemUiModel) -> Unit
) : ListAdapter<CardItemUiModel, CardsListAdapter.CardViewHolder>(CardsDiffUtilItemCallback()) {

    @SuppressLint("NotifyDataSetChanged")
    fun setCards(cards: List<CardItemUiModel>) {
        submitList(cards)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.acq_card_list_item, parent, false) as View
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteClick, onChooseClick)
    }

    override fun onBindViewHolder(
        holder: CardViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains(PAYLOAD_CHANGE_MODE)) {
            holder.bindDeleteVisibility(getItem(position).showDelete)
            holder.bindChooseVisibility(getItem(position).showChoose)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val cardLogo =
            itemView.findViewById<ImageView>(R.id.acq_card_list_item_logo)
        private val cardNameView =
            itemView.findViewById<TextView>(R.id.acq_card_list_item_masked_name)
        private val cardDeleteIcon =
            itemView.findViewById<ImageView>(R.id.acq_card_list_item_delete)
        private val cardChooseIcon =
            itemView.findViewById<ImageView>(R.id.acq_card_list_item_choose)

        fun bind(
            card: CardItemUiModel,
            onDeleteClick: (CardItemUiModel) -> Unit,
            onChooseClick: (CardItemUiModel) -> Unit
        ) {
            cardLogo.setImageResource(CardLogoProvider.getCardLogo(card.pan))
            cardNameView.text = itemView.context.getString(
                R.string.acq_cardlist_bankname,
                card.bankName.orEmpty(),
                card.tail
            )
            cardChooseIcon.isVisible = card.showChoose
            cardDeleteIcon.isVisible = card.showDelete
            cardDeleteIcon.setOnClickListener { onDeleteClick(card) }
            itemView.setOnClickListener {
                onChooseClick(card)
            }
        }

        fun bindDeleteVisibility(showDelete: Boolean) {
            cardDeleteIcon.isVisible = showDelete
        }

        fun bindChooseVisibility(showChosen: Boolean) {
            cardChooseIcon.isVisible = showChosen
        }
    }

    companion object {
        const val PAYLOAD_CHANGE_MODE = "PAYLOAD_CHANGE_MODE"
    }
}

private class CardsDiffUtilItemCallback : DiffUtil.ItemCallback<CardItemUiModel>() {

    override fun areItemsTheSame(oldItem: CardItemUiModel, newItem: CardItemUiModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CardItemUiModel, newItem: CardItemUiModel): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: CardItemUiModel, newItem: CardItemUiModel): Any? {
        return if (oldItem.showDelete != newItem.showDelete || oldItem.showChoose != newItem.showChoose) {
            PAYLOAD_CHANGE_MODE
        } else null
    }
}
