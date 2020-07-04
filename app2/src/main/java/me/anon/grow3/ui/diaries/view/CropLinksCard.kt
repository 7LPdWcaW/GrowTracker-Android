package me.anon.grow3.ui.diaries.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardCropLinksBinding
import me.anon.grow3.view.model.Card

class CropLinksCard(
	val diary: Diary,
	val crop: Crop,
	title: String? = null
) : Card<CardCropLinksBinding>(title)
{
	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardCropLinksBinding
		= CardCropLinksBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardCropLinksBinding = CardCropLinksBinding.bind(view)

	override fun bind(view: CardCropLinksBinding)
	{

	}
}
