package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log

abstract class LogView<T : Log>(
	val diary: Diary,
	val log: T
)
{
	abstract fun createView(inflater: LayoutInflater, parent: ViewGroup): View
	abstract fun bindView(view: View)
	abstract fun saveView(): T
	open fun provideTitle(): String? = null
}
