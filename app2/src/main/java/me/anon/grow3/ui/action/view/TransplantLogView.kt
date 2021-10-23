package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.data.model.Transplant
import me.anon.grow3.databinding.FragmentActionLogTransplantBinding

class TransplantLogView : LogView<FragmentActionLogTransplantBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: Transplant

	constructor() : super()
	constructor(diary: Diary, log: Transplant) : super()
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): FragmentActionLogTransplantBinding
		= FragmentActionLogTransplantBinding.inflate(inflater, parent, false)

	override fun bindView(view: View): FragmentActionLogTransplantBinding
		= FragmentActionLogTransplantBinding.bind(view)

	override fun bind(view: FragmentActionLogTransplantBinding)
	{

	}

	override fun save(view: FragmentActionLogTransplantBinding): Log
	{
		view.root.clearFocus()
		return log
	}

	override fun provideTitle(): String = ""
}
