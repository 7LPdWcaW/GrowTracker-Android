package me.anon.grow3.ui.crops.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardCropBinding
import me.anon.grow3.view.model.Card

class CropCard : Card<CardCropBinding>
{
	private lateinit var diary: Diary
	private lateinit var crop: Crop

	constructor() : super()
	constructor(diary: Diary, crop: Crop) : super()
	{
		this.diary = diary
		this.crop = crop
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardCropBinding
		= CardCropBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardCropBinding = CardCropBinding.bind(view)

	override fun bind(view: CardCropBinding)
	{

	}
}
