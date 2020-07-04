package me.anon.grow3.ui.diaries.view

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

class LogMediumCard(
	val diary: Diary,
	val crop: Crop,
	val medium: Medium,
	title: String? = null
) : Card<CardLogMediumBinding>(title)
{
	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardLogMediumBinding
		= CardLogMediumBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardLogMediumBinding = CardLogMediumBinding.bind(view)

	override fun bind(view: CardLogMediumBinding)
	{
		view.lastMediumContainer.isVisible = true
		view.mediumContent.text = medium.summary()
		view.mediumDate.text = R.string.days.string(medium.date.nowDifferenceDays()) +
			"/" +
			diary.stageWhen(crop, medium).transform {
				"" + this.days + this.stage.type.strRes.string()[0]
			}
	}
}
