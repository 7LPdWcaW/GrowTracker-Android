package me.anon.grow3.ui.crops.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.shortSummary
import me.anon.grow3.databinding.CardCropBinding
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.crops.fragment.ViewCropFragment
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onClick
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

	inner class CropCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= CropCardHolder(CardCropBinding.inflate(inflater, parent, false).root)

	override fun bindView(view: View): CardCropBinding = CardCropBinding.bind(view)

	override fun bind(view: CardCropBinding)
	{
		view.name.text = crop.name
		view.summary.text = diary.stagesOf(crop).shortSummary()

		view.root.onClick {
			it.navigateTo<ViewCropFragment> {
				bundleOf(
					Extras.EXTRA_DIARY_ID to diary.id,
					Extras.EXTRA_CROP_ID to crop.id
				)
			}
		}
	}
}
