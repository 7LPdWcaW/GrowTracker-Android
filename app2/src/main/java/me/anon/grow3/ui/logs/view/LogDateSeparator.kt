package me.anon.grow3.ui.logs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.databinding.CardLogDateSeparatorBinding
import me.anon.grow3.view.model.Card

class LogDateSeparator : Card<CardLogDateSeparatorBinding>
{
	private lateinit var date: String

	constructor() : super(null)
	constructor(date: String, title: String? = null) : super(title)
	{
		this.date = date
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardLogDateSeparatorBinding
		= CardLogDateSeparatorBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardLogDateSeparatorBinding = CardLogDateSeparatorBinding.bind(view)

	override fun bind(view: CardLogDateSeparatorBinding)
	{
		view.date.text = date
	}
}
