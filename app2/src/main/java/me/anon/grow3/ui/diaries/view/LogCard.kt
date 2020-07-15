package me.anon.grow3.ui.diaries.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.databinding.CardLogBinding
import me.anon.grow3.view.model.Card

class LogCard : Card<CardLogBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: Log

	constructor() : super(null)
	constructor(diary: Diary, log: Log, title: String? = null) : super(title)
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardLogBinding
		= CardLogBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardLogBinding = CardLogBinding.bind(view)

	override fun bind(view: CardLogBinding)
	{
		view.header.text = log.summary()
	}
}
