package me.anon.grow3.ui.common.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardStagesBinding
import me.anon.grow3.view.model.Card

class StagesCard : Card<CardStagesBinding>
{
	private lateinit var diary: Diary
	private var crop: Crop? = null

	constructor() : super(null)
	constructor(diary: Diary, crop: Crop? = null, title: String? = null) : super(title)
	{
		this.diary = diary
		this.crop = crop
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardStagesBinding
		= CardStagesBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardStagesBinding = CardStagesBinding.bind(view)

	override fun bind(view: CardStagesBinding)
	{
		view.stagesHeader.text = title
		view.stagesHeader.isVisible = !title.isNullOrBlank()

		if (crop != null)
		{
			view.stagesView.setStages(diary, crop!!)
		}
		else
		{
			view.stagesView.setStages(diary)
		}

		view.stagesView.isNestedScrollingEnabled = true
	}
}
