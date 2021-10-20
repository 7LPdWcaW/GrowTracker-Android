package me.anon.grow3.view.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * View model for displaying a card within the [CardListAdapter].
 * Subclasses must contain a no-args constructor.
 */
abstract class Card<T : ViewBinding>
{
	abstract class CardViewHolder(view: View) : RecyclerView.ViewHolder(view)
	{
		open fun bind(card: Card<ViewBinding>)
		{
			card.bindAdapter(itemView)
		}
	}

	abstract fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder

	/**
	 * Binds the provided view into the view binding instance
	 */
	abstract fun bindView(view: View): T

	/**
	 * No-use. This method is called by the adapter to correctly typecast
	 */
	public fun bindAdapter(view: View)
		= bind(bindView(view))

	/**
	 * Binds the card to the view
	 */
	abstract fun bind(view: T)
}
