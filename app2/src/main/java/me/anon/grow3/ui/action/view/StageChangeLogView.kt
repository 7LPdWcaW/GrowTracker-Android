package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.data.model.StageType
import me.anon.grow3.databinding.FragmentActionLogStageChangeBinding

class StageChangeLogView : LogView<FragmentActionLogStageChangeBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: StageChange

	constructor() : super()
	constructor(diary: Diary, log: StageChange) : super()
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): FragmentActionLogStageChangeBinding
		= FragmentActionLogStageChangeBinding.inflate(inflater, parent, false)

	override fun bindView(view: View): FragmentActionLogStageChangeBinding
		= FragmentActionLogStageChangeBinding.bind(view)

	override fun bind(view: FragmentActionLogStageChangeBinding)
	{
		view.stages.setMenu(StageType.toMenu())
		view.stages.singleSelection = true
		view.stages.checkItems(diary.stage().type.strRes)
		view.stages.itemSelectListener = { item ->

		}
	}

	override fun save(view: FragmentActionLogStageChangeBinding): StageChange
	{
		view.root.clearFocus()
		log.type = StageType.ofId(view.stages.getSelectedItems().first().itemId)
		return log
	}

	override fun provideTitle(): String = "Plant stage change"
}
