package me.anon.grow3.ui.logs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.plusAssign
import me.anon.grow3.R
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Water
import me.anon.grow3.databinding.CardWaterLogBinding
import me.anon.grow3.databinding.StubDataLabelBinding
import me.anon.grow3.util.asString
import me.anon.grow3.util.inflate
import me.anon.grow3.util.string
import me.anon.grow3.view.model.Card

class WaterLogCard : Card<CardWaterLogBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: Water

	constructor() : super(null)
	constructor(diary: Diary, log: Water, title: String? = null) : super(title)
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardWaterLogBinding
		= CardWaterLogBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardWaterLogBinding = CardWaterLogBinding.bind(view)

	override fun bind(view: CardWaterLogBinding)
	{
		view.stubCardHeader.header.text = log.summary()
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
	}
}
