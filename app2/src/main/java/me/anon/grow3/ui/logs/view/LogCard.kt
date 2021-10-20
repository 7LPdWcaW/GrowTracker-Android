package me.anon.grow3.ui.logs.view

import androidx.viewbinding.ViewBinding
import me.anon.grow3.R
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.view.LogFooterView
import me.anon.grow3.view.LogHeaderView
import me.anon.grow3.view.model.Card

abstract class LogCard<T : ViewBinding, L: Log> : Card<T>
{
	protected lateinit var diary: Diary
	protected lateinit var log: L

	constructor() : super()
	constructor(diary: Diary, log: L) : super()
	{
		this.diary = diary
		this.log = log
	}

	final override fun bind(view: T)
	{
		view.root.findViewById<LogHeaderView>(R.id.header).setLog(diary, log)
		view.root.findViewById<LogFooterView>(R.id.footer).setLog(diary, log)
		bindLog(view)
	}

	abstract fun bindLog(view: T)
}