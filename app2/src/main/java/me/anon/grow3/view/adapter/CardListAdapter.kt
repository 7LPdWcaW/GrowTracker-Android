package me.anon.grow3.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import me.anon.grow3.di.Cards
import me.anon.grow3.view.model.Card
import javax.inject.Inject

class CardListAdapter @Inject constructor(
	@Cards val types: Array<Class<out Card<*>>>
) : RecyclerView.Adapter<Card.CardViewHolder>()
{
	private val cards: ArrayList<Card<*>> = arrayListOf()

	/**
	 * Clears the stack of cards
	 */
	public fun clearStack()
	{
		val size = cards.size
		cards.clear()
		this.notifyItemRangeRemoved(0, size)
	}

	/**
	 * Clears the adapter and provides a new stack of cards to display
	 */
	@SuppressLint("NotifyDataSetChanged")
	public fun newStack(block: ArrayList<Card<*>>.() -> Unit)
	{
		cards.clear()
		block(this.cards)
		this.notifyDataSetChanged()
	}

	override fun getItemViewType(position: Int): Int = types.indexOf(cards[position]::class.java)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Card.CardViewHolder
	{
		return types[viewType].newInstance()
			.createViewHolder(LayoutInflater.from(parent.context), parent)
	}

	override fun getItemCount(): Int = cards.size

	override fun onBindViewHolder(holder: Card.CardViewHolder, position: Int)
	{
		holder.bind(cards[position] as Card<ViewBinding>)
	}
}
