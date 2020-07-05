package me.anon.grow3.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow3.view.model.Card

open class CardListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
	public val cards: ArrayList<Card<*>> = arrayListOf()

	override fun getItemViewType(position: Int): Int = position

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
		= object : RecyclerView.ViewHolder(cards[viewType].createView(LayoutInflater.from(parent.context), parent).root) {}

	override fun getItemCount(): Int = cards.size

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
	{
		cards[position]._bindView(holder.itemView)
	}
}
