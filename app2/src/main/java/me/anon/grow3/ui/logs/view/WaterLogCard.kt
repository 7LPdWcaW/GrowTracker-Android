package me.anon.grow3.ui.logs.view

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.plusAssign
import me.anon.grow3.R
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Water
import me.anon.grow3.databinding.CardWaterLogBinding
import me.anon.grow3.databinding.StubCropSmallBinding
import me.anon.grow3.databinding.StubDataLabelBinding
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.crops.fragment.ViewCropFragment
import me.anon.grow3.util.*
import me.anon.grow3.view.model.Card

class WaterLogCard : Card<CardWaterLogBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: Water

	constructor() : super()
	constructor(diary: Diary, log: Water) : super()
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardWaterLogBinding
		= CardWaterLogBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardWaterLogBinding = CardWaterLogBinding.bind(view)

	override fun bind(view: CardWaterLogBinding)
	{
		view.includeStubCardHeader.header.text = "Watered"
		view.includeStubCardHeader.date.text = "${log.date.asDateTime().formatTime()} - " + diary.stageWhen(log).transform {
			toString() + "/" + total
		}

		view.content.removeAllViews()

		log.inPH?.let { ph ->
			view.content += view.content.inflate<View>(R.layout.stub_data_label).apply {
				val binding = StubDataLabelBinding.bind(this)
				binding.data.text = ph.amount.asString()
				binding.label.text = R.string.log_water_field_inputph.string()
			}
		}

		log.outPH?.let { ph ->
			view.content += view.content.inflate<View>(R.layout.stub_data_label).apply {
				val binding = StubDataLabelBinding.bind(this)
				binding.data.text = ph.amount.asString()
				binding.label.text = R.string.log_water_field_outputph.string()
			}
		}

		view.cropsContainer.removeAllViews()
		log.cropIds
			.mapToView<String, StubCropSmallBinding>(view.cropsContainer) { cropId, cropView ->
				val crop = diary.crop(cropId)
				cropView.cropImage.onClick {
					it.navigateTo<ViewCropFragment> {
						bundleOf(
							Extras.EXTRA_DIARY_ID to diary.id,
							Extras.EXTRA_CROP_ID to cropId
						)
					}
				}
			}
			.hideIfEmpty()

		view.root.onClick {
			val menu = PopupMenu(it.context, it, Gravity.BOTTOM or Gravity.END)
			menu.inflate(R.menu.menu_sample)
			menu.show()
		}
	}
}
