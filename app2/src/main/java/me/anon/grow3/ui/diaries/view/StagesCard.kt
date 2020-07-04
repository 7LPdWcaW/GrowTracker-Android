package me.anon.grow3.ui.diaries.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardStagesBinding
import me.anon.grow3.view.model.Card

class StagesCard(
	val diary: Diary,
	val crop: Crop?,
	title: String? = null
) : Card<CardStagesBinding>(title)
{
	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardStagesBinding
		= CardStagesBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardStagesBinding = CardStagesBinding.bind(view)

	override fun bind(view: CardStagesBinding)
	{
		view.stagesHeader.text = title
		view.stagesHeader.isVisible = !title.isNullOrBlank()

		if (crop != null)
		{
			view.stagesView.setStages(diary, crop)
		}
		else
		{
			view.stagesView.setStages(diary)
		}

		view.stagesView.isNestedScrollingEnabled = true
	}
}
