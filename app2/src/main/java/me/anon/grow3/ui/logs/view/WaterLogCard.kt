package me.anon.grow3.ui.logs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.plusAssign
import androidx.core.view.updatePadding
import me.anon.grow3.R
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Water
import me.anon.grow3.databinding.CardWaterLogBinding
import me.anon.grow3.databinding.StubDataLabelBinding
import me.anon.grow3.util.*

class WaterLogCard : LogCard<CardWaterLogBinding, Water>
{
	constructor() : super()
	constructor(diary: Diary, log: Water) : super(diary, log)

	inner class WaterLogCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= WaterLogCardHolder(CardWaterLogBinding.inflate(inflater, parent, false).root)

	override fun bindView(view: View): CardWaterLogBinding = CardWaterLogBinding.bind(view)

	override fun bindLog(view: CardWaterLogBinding)
	{
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

		log.tds?.let { tds ->
			view.content += view.content.inflate<View>(R.layout.stub_data_label).apply {
				val binding = StubDataLabelBinding.bind(this)
				binding.data.text = "${tds.amount.asString()}mS/cm"
				binding.label.text = R.string.log_water_field_tds.string()
			}
		}

		log.amount?.let { amount ->
			view.content += view.content.inflate<View>(R.layout.stub_data_label).apply {
				val binding = StubDataLabelBinding.bind(this)
				binding.data.text = "${amount.amount.asString()}L"
				binding.label.text = R.string.log_water_field_amount.string()
			}
		}

		log.temperature?.let { temp ->
			view.content += view.content.inflate<View>(R.layout.stub_data_label).apply {
				val binding = StubDataLabelBinding.bind(this)
				binding.data.text = "${temp.asString()}℃"
				binding.label.text = R.string.log_water_field_temperature.string()
			}
		}

		view.content.hideIfEmpty()

		view.additivesContainer.removeAllViews()
		log.additives
			.filter { it.description.isNotEmpty() }
			.mapToView<Water.Additive, StubDataLabelBinding>(view.additivesContainer) { additive, dataView ->
				dataView.data.text = "${additive.amount}ml/l"
				dataView.label.text = "• ${additive.description}: "
				dataView.root.updatePadding(left = 16.dp)
			}
			.hideIfEmpty()
	}
}
