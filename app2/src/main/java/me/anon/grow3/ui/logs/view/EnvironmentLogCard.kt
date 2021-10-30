package me.anon.grow3.ui.logs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Environment
import me.anon.grow3.databinding.CardEnvironmentLogBinding

class EnvironmentLogCard : LogCard<CardEnvironmentLogBinding, Environment>
{
	constructor() : super()
	constructor(diary: Diary, log: Environment) : super(diary, log)

	inner class EnvironmentLogCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= EnvironmentLogCardHolder(CardEnvironmentLogBinding.inflate(inflater, parent, false).root)

	override fun bindView(view: View): CardEnvironmentLogBinding = CardEnvironmentLogBinding.bind(view)

	override fun bindLog(view: CardEnvironmentLogBinding)
	{

	}
}
