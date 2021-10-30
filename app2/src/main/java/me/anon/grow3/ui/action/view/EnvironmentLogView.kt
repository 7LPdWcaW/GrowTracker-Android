package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Environment
import me.anon.grow3.data.model.Log
import me.anon.grow3.databinding.FragmentActionLogEnvironmentBinding

class EnvironmentLogView : LogView<FragmentActionLogEnvironmentBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: Environment

	constructor() : super()
	constructor(diary: Diary, log: Environment) : super()
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): FragmentActionLogEnvironmentBinding
		= FragmentActionLogEnvironmentBinding.inflate(inflater, parent, false)

	override fun bindView(view: View): FragmentActionLogEnvironmentBinding
		= FragmentActionLogEnvironmentBinding.bind(view)

	override fun bind(view: FragmentActionLogEnvironmentBinding)
	{

	}

	override fun save(view: FragmentActionLogEnvironmentBinding): Log
	{
		view.root.clearFocus()


		return log
	}

	override fun provideTitle(): String = "Environment change"
}
