package me.anon.grow3.ui.common.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import me.anon.grow3.R
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Medium
import me.anon.grow3.databinding.CardLogMediumBinding
import me.anon.grow3.util.nowDifferenceDays
import me.anon.grow3.util.string
import me.anon.grow3.util.transform
import me.anon.grow3.view.model.Card

class LogMediumCard : Card<CardLogMediumBinding>
{
	private var title: String? = null
	private lateinit var diary: Diary
	private lateinit var crop: Crop
	private lateinit var medium: Medium

	constructor() : super()
	constructor(diary: Diary, crop: Crop, medium: Medium, title: String? = null) : super()
	{
		this.title = title
		this.diary = diary
		this.crop = crop
		this.medium = medium
	}

	inner class LogMediumCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= LogMediumCardHolder(CardLogMediumBinding.inflate(inflater, parent, false).root)

	override fun bindView(view: View): CardLogMediumBinding = CardLogMediumBinding.bind(view)

	override fun bind(view: CardLogMediumBinding)
	{
		view.mediumHeader.text = title
		view.mediumHeader.isVisible = !title.isNullOrBlank()

		view.mediumContent.text = medium.summary()
		view.mediumDate.text = R.string.days.string(medium.date.nowDifferenceDays()) +
			"/" +
			diary.stageWhen(crop, medium).transform {
				"" + this.days + this.stage.type.strRes.string()[0]
			}
	}
}
