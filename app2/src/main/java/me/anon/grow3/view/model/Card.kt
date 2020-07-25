package me.anon.grow3.view.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * View model for displaying a card within the [CardListAdapter].
 * Subclasses must contain a no-args constructor and [createView] must
 * only return the inflated view.
 */
abstract class Card<T : ViewBinding>(
	var title: String? = null
)
{
	/**
	 * Inflates the view for the adapter. This must not contain other UI or
	 * model logic as it may be called from a different instance
	 */
	abstract fun createView(inflater: LayoutInflater, parent: ViewGroup): T

	/**
	 * Binds the provided view into the view binding instance
	 */
	abstract fun bindView(view: View): T

	/**
	 * No-use. This method is called by the adapter to correctly typecast
	 */
	public fun _bindView(view: View)
	{
		bind(bindView(view))
	}

	/**
	 * Binds the card to the view
	 */
	abstract fun bind(view: T)
}
