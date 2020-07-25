package me.anon.grow3.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow3.util.uniqueBy
import me.anon.grow3.view.model.Card

open class CardListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
	private val cards: ArrayList<Card<*>> = arrayListOf()
	private val cardTypes: ArrayList<Class<out Card<*>>> = arrayListOf()

	/**
	 * Clears the stack of cards
	 */
	public fun clearStack()
	{
		cards.clear()
		cardTypes.clear()
		this.notifyDataSetChanged()
	}

	/**
	 * Clears the adapter and provides a new stack of cards to display
	 */
	public fun newStack(block: ArrayList<Card<*>>.() -> Unit)
	{
		cards.clear()
		block(this.cards)
		cardTypes.clear()
		cardTypes.addAll(cards.map { it::class.java }.uniqueBy { it })
		this.notifyDataSetChanged()
	}

	override fun getItemViewType(position: Int): Int = cardTypes.indexOf(cards[position]::class.java)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
	{
		val binder = cardTypes[viewType].newInstance()
			.createView(LayoutInflater.from(parent.context), parent)

		return object : RecyclerView.ViewHolder(binder.root) {}
	}

	override fun getItemCount(): Int = cards.size

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
	{
		cards[position]._bindView(holder.itemView)
	}
}
