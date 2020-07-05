package me.anon.grow3.ui.crops.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardCropDetailsBinding
import me.anon.grow3.util.asEditable
import me.anon.grow3.view.model.Card

class CropDetailsCard(
	val diary: Diary,
	val crop: Crop,
	title: String? = null
) : Card<CardCropDetailsBinding>(title)
{
	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardCropDetailsBinding
		= CardCropDetailsBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardCropDetailsBinding = CardCropDetailsBinding.bind(view)

	override fun bind(view: CardCropDetailsBinding)
	{
		view.cropName.editText!!.text = crop.name.asEditable()
		view.cropGenetics.editText!!.text = crop.genetics?.asEditable()
		view.cropNumPlants.editText!!.text = crop.numberOfPlants.toString().asEditable()
	}
}
