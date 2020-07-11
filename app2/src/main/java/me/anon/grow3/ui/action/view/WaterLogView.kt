package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Water
import me.anon.grow3.databinding.FragmentActionLogWaterBinding
import me.anon.grow3.util.asEditable
import me.anon.grow3.util.asStringOrNull
import me.anon.grow3.util.onFocusLoss
import me.anon.grow3.util.toDoubleOrNull

class WaterLogView(
	log: Water
) : LogView<Water>(log)
{
	private lateinit var bindings: FragmentActionLogWaterBinding

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): View
		= FragmentActionLogWaterBinding.inflate(inflater, parent, false).root

	override fun bindView(view: View)
	{
		bindings = FragmentActionLogWaterBinding.bind(view)

		bindings.waterPh.editText!!.onFocusLoss {
			log.inPH = Water.PHUnit(it.text.toDoubleOrNull())
		}

		bindings.waterRunoff.editText!!.onFocusLoss {
			log.outPH = Water.PHUnit(it.text.toDoubleOrNull())
		}

		bindings.waterPh.editText!!.text = log.inPH?.amount.asStringOrNull()?.asEditable()
		bindings.waterRunoff.editText!!.text = log.outPH?.amount.asStringOrNull()?.asEditable()
	}

	override fun saveView()
	{
		log.cropIds = bindings.cropSelectView.selectedCrops.map { it.id }.toList()
	}

	override fun provideTitle(): String? = "Edit water log"
}
