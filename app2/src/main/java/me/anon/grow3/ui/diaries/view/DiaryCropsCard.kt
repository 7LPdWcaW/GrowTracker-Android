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
import me.anon.grow3.ui.crops.fragment.CropListFragment
import me.anon.grow3.ui.crops.fragment.ViewCropFragment
import me.anon.grow3.util.mapToView
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onClick
import me.anon.grow3.view.model.Card

class DiaryCropsCard : Card<CardDiaryCropsBinding>
{
	private var title: String? = null
	private lateinit var diary: Diary

	constructor() : super()
	constructor(diary: Diary, title: String? = null) : super()
	{
		this.title = title
		this.diary = diary
	}

	inner class DiaryCropsCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= DiaryCropsCardHolder(CardDiaryCropsBinding.inflate(inflater, parent, false).root)

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

		// TODO: Hide when there's an overflow
		view.moreCrops.isVisible = false
		view.cropsContainer.post {
			view.moreCrops.isVisible = view.cropsContainer.totalItemDisplayed < diary.crops.size
			view.moreCrops.onClick {
				it.navigateTo<CropListFragment> {
					bundleOf(Extras.EXTRA_DIARY_ID to diary.id)
				}
			}
		}
	}
}
