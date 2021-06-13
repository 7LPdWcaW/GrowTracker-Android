package me.anon.grow3.ui.crops.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardCropLinksBinding
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.logs.fragment.LogListFragment
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onClick
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
		view.viewLogs.onClick {
			it.navigateTo<LogListFragment> {
				bundleOf(
					Extras.EXTRA_DIARY_ID to diary.id,
					Extras.EXTRA_CROP_IDS to arrayOf(crop.id)
				)
			}
		}
	}
}
