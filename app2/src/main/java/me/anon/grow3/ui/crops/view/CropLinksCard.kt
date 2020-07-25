package me.anon.grow3.ui.crops.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardCropLinksBinding
import me.anon.grow3.view.model.Card

class CropLinksCard : Card<CardCropLinksBinding>
{
	private lateinit var diary: Diary
	private lateinit var crop: Crop

	constructor() : super()
	constructor(diary: Diary, crop: Crop) : super()
	{
		this.diary = diary
		this.crop = crop
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardCropLinksBinding
		= CardCropLinksBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardCropLinksBinding = CardCropLinksBinding.bind(view)

	override fun bind(view: CardCropLinksBinding)
	{

	}
}
