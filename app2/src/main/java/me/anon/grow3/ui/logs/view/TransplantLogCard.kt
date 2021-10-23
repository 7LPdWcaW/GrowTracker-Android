package me.anon.grow3.ui.logs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Medium
import me.anon.grow3.databinding.CardTransplantLogBinding
import me.anon.grow3.util.string

class TransplantLogCard : LogCard<CardTransplantLogBinding, Medium>
{
	constructor() : super()
	constructor(diary: Diary, log: Medium) : super(diary, log)

	inner class TransplantLogCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= TransplantLogCardHolder(CardTransplantLogBinding.inflate(inflater, parent, false).root)

	override fun bindView(view: View): CardTransplantLogBinding = CardTransplantLogBinding.bind(view)

	override fun bindLog(view: CardTransplantLogBinding)
	{
		val comp = arrayListOf<String>()
		comp.add(log.medium.strRes.string())

		log.size?.let { size ->
			comp.add("${size.amount} ${size.unit.strRes.string()}")
		}

		view.content.text = comp.joinToString()
	}
}
