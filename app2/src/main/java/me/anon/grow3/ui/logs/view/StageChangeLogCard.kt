package me.anon.grow3.ui.logs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.StageAt
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.data.model.StageType
import me.anon.grow3.databinding.CardStagechangeLogBinding
import me.anon.grow3.util.string

class StageChangeLogCard : LogCard<CardStagechangeLogBinding, StageChange>
{
	private lateinit var previousStage: StageAt

	constructor() : super()
	constructor(diary: Diary, log: StageChange) : super(diary, log)
	{
		this.previousStage = diary.stageWhen(log)
	}

	inner class StageChangeLogCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= StageChangeLogCardHolder(CardStagechangeLogBinding.inflate(inflater, parent, false).root)

	override fun bindView(view: View): CardStagechangeLogBinding = CardStagechangeLogBinding.bind(view)

	override fun bindLog(view: CardStagechangeLogBinding)
	{
		if (log.type == StageType.Started)
		{
			view.header.title.text = "Diary started"
			view.content.text = ""
			view.content.isVisible = false
		}
		else
		{
			view.content.isVisible = true
			if (previousStage.stage.type == log.type)
			{
				view.content.text = "Stage set to ${log.type.strRes.string()}"
			}
			else
			{
				view.content.text = "Changed from ${previousStage.stage.type.strRes.string()} to ${log.type.strRes.string()}"
			}
		}
	}
}
