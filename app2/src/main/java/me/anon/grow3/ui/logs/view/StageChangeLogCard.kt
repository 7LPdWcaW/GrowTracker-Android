package me.anon.grow3.ui.logs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.StageAt
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.databinding.CardStagechangeLogBinding
import me.anon.grow3.util.asDateTime
import me.anon.grow3.util.formatTime
import me.anon.grow3.util.string
import me.anon.grow3.view.model.Card

class StageChangeLogCard : Card<CardStagechangeLogBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: StageChange
	private lateinit var previousStage: StageAt

	constructor() : super()
	constructor(diary: Diary, log: StageChange) : super()
	{
		this.diary = diary
		this.log = log
		this.previousStage = diary.stageWhen(log)
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardStagechangeLogBinding
		= CardStagechangeLogBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardStagechangeLogBinding = CardStagechangeLogBinding.bind(view)

	override fun bind(view: CardStagechangeLogBinding)
	{
		view.includeStubCardHeader.header.text = "Stage change"
		view.includeStubCardHeader.date.text = "${log.date.asDateTime().formatTime()}"

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
