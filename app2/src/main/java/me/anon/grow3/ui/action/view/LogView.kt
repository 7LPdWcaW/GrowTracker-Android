package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Log

abstract class LogView<T : Log>(
	val log: T
)
{
	abstract fun createView(inflater: LayoutInflater, parent: ViewGroup): View
	abstract fun bindView(view: View)
}
