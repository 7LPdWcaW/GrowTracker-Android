package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.*
import me.anon.grow3.databinding.FragmentActionLogWaterBinding
import me.anon.grow3.util.asEditable
import me.anon.grow3.util.asStringOrNull
import me.anon.grow3.util.onFocusLoss
import me.anon.grow3.util.toDoubleOrNull

class WaterLogView(
	diary: Diary,
	log: Water
) : LogView<Water>(diary, log)
{
	private lateinit var bindings: FragmentActionLogWaterBinding

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): View
		= FragmentActionLogWaterBinding.inflate(inflater, parent, false).root

	override fun bindView(view: View)
	{
		bindings = FragmentActionLogWaterBinding.bind(view)

		bindings.waterPh.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.inPH = Water.PHUnit(it) }
		}

		bindings.waterRunoff.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.outPH = Water.PHUnit(it) }
		}

		bindings.waterAmount.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.amount = Volume(it, VolumeUnit.L) }
		}
		bindings.waterTds.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.tds = Water.TdsUnit(it, TdsType.EC) }
		}
		bindings.waterTemp.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.temperature = it }
		}

		bindings.waterPh.editText!!.text = log.inPH?.amount.asStringOrNull()?.asEditable()
		bindings.waterRunoff.editText!!.text = log.outPH?.amount.asStringOrNull()?.asEditable()
		bindings.waterAmount.editText!!.text = log.amount?.amount.asStringOrNull()?.asEditable()
		bindings.waterTds.editText!!.text = log.tds?.amount.asStringOrNull()?.asEditable()
		bindings.waterTemp.editText!!.text = log.temperature?.asStringOrNull()?.asEditable()
	}

	override fun saveView(): Water
	{
		bindings.root.clearFocus()
		return log
	}

	override fun provideTitle(): String? = "Edit water log"
}
