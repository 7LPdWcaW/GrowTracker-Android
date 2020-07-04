package me.anon.grow3.view.model

import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding

abstract class Card<T : ViewBinding>(
	var title: String? = null
)
{
	abstract fun createView(inflater: LayoutInflater): T
	abstract fun bindView(view: View): T

	public fun _bindView(view: View)
	{
		bind(bindView(view))
	}

	abstract fun bind(view: T)
}
