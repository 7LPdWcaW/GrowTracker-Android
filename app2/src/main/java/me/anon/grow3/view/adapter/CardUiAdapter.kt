package me.anon.grow3.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow3.view.model.Card
import me.anon.grow3.view.viewholder.CardViewHolder

open class CardUiAdapter : RecyclerView.Adapter<CardViewHolder>()
{
	public val cards: ArrayList<Card<*>> = arrayListOf()

	override fun getItemViewType(position: Int): Int = position

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder
		= CardViewHolder(cards[viewType].createView(LayoutInflater.from(parent.context)).root)

	override fun getItemCount(): Int = cards.size

	override fun onBindViewHolder(holder: CardViewHolder, position: Int)
	{
		cards[position]._bindView(holder.itemView)
	}
}
