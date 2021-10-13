package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.ui.action.fragment.LogActionFragment

abstract class LogView<T : Log>(
	val diary: Diary,
	val log: T
)
{
	public lateinit var fragment: LogActionFragment

	abstract fun createView(inflater: LayoutInflater, parent: ViewGroup): View
	abstract fun bindView(view: View)
	abstract fun saveView(): T
	open fun provideTitle(): String? = null
}
