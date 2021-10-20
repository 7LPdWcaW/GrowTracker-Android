package me.anon.grow3.ui.crops.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardCropDetailsBinding
import me.anon.grow3.util.asEditable
import me.anon.grow3.view.model.Card

class CropDetailsCard : Card<CardCropDetailsBinding>
{
	private lateinit var diary: Diary
	private lateinit var crop: Crop

	constructor() : super()
	constructor(diary: Diary, crop: Crop) : super()
	{
		this.diary = diary
		this.crop = crop
	}

	inner class CropDetailsCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= CropDetailsCardHolder(CardCropDetailsBinding.inflate(inflater, parent, false).root)

	override fun bindView(view: View): CardCropDetailsBinding = CardCropDetailsBinding.bind(view)

	override fun bind(view: CardCropDetailsBinding)
	{
		view.cropName.editText!!.text = crop.name.asEditable()
		view.cropGenetics.editText!!.text = crop.genetics?.asEditable()
		view.cropNumPlants.editText!!.text = crop.numberOfPlants.toString().asEditable()
	}
}
