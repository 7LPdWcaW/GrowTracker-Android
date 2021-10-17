package me.anon.grow3.ui.common.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.databinding.CardStagesBinding
import me.anon.grow3.ui.action.fragment.LogActionBottomSheetFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.nameOf
import me.anon.grow3.util.navigateTo
import me.anon.grow3.view.model.Card

class StagesCard : Card<CardStagesBinding>
{
	private var title: String? = null
	private lateinit var diary: Diary
	private var crop: Crop? = null

	constructor() : super()
	constructor(diary: Diary, crop: Crop? = null, title: String? = null) : super()
	{
		this.title = title
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
		view.stagesView.onNewStageClick = {
			it.navigateTo<LogActionBottomSheetFragment>(true) {
				bundleOf(
					Extras.EXTRA_DIARY_ID to diary.id,
					Extras.EXTRA_LOG_TYPE to nameOf<StageChange>()
				)
			}
		}

		view.stagesView.setStages(diary, crop)
		view.stagesView.isNestedScrollingEnabled = true
	}
}
