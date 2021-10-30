package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import me.anon.grow3.data.model.Log

abstract class LogView<T : ViewBinding>
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
	public fun bindAdapter(view: View)
		= bind(bindView(view))

	/**
	 * No-use. This method is called by the adapter to correctly typecast
	 */
	public fun saveAdapter(view: View): Log
		= save(bindView(view))

	/**
	 * Binds the card to the view
	 */
	abstract fun bind(view: T)
	abstract fun save(view: T): Log

	open fun provideTitle(): String? = null
}
