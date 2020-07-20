package me.anon.grow3.ui.diaries.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardDiaryCropsBinding
import me.anon.grow3.databinding.StubCropBinding
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.crops.fragment.ViewCropFragment
import me.anon.grow3.util.mapToView
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onClick
import me.anon.grow3.view.model.Card

class DiaryCropsCard : Card<CardDiaryCropsBinding>
{
	private lateinit var diary: Diary

	constructor() : super(null)
	constructor(diary: Diary, title: String? = null) : super(title)
	{
		this.diary = diary
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardDiaryCropsBinding
		= CardDiaryCropsBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardDiaryCropsBinding = CardDiaryCropsBinding.bind(view)

	override fun bind(view: CardDiaryCropsBinding)
	{
		view.cropsTitle.text = title
		view.cropsTitle.isVisible = !title.isNullOrBlank()

		view.cropsContainer.removeAllViews()
		diary.crops.mapToView<Crop, StubCropBinding>(container = view.cropsContainer, mapper = { crop, view ->
			view.cropName.text = crop.name
			view.cropImage.onClick {
				it.navigateTo<ViewCropFragment>() {
					bundleOf(
						Extras.EXTRA_DIARY_ID to diary.id,
						Extras.EXTRA_CROP_ID to crop.id
					)
				}
			}
		})
	}
}
