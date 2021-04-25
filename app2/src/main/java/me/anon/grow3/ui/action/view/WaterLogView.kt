package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.plusAssign
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import me.anon.grow3.data.model.*
import me.anon.grow3.databinding.FragmentActionLogWaterBinding
import me.anon.grow3.databinding.StubLogWaterAdditiveBinding
import me.anon.grow3.util.*

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
		bindings.waterAmount.editText!!.doAfterTextChanged { text ->
			updateAdditiveTotals(text?.toDoubleOrNull() ?: 0.0)
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

		addAdditiveView(bindings.additivesContainer)
	}

	private fun updateAdditiveTotals(amount: Double)
	{
		bindings.additivesContainer.childViews.forEach { child ->
			val child = StubLogWaterAdditiveBinding.bind(child)
			val total = (child.amountEdit.text!!.toDoubleOrNull() ?: 0.0) * amount
			child.totals.text = "@${total.asStringOrNull()}ml"
			child.totals.isVisible = child.totals.text.isNotEmpty()
		}
	}

	private fun addAdditiveView(container: ViewGroup): View
	{
		val additive = Water.Additive(amount = 0.0)
		log.additives += additive

		val additiveView = StubLogWaterAdditiveBinding.inflate(LayoutInflater.from(container.context), bindings.additivesContainer, false)
		val textChangeListener = { text: CharSequence?, start: Int, count: Int, after: Int ->
			// remove last empty inputs
			var emptyCount = 0
			var emptyIndex = 0
			container.childViews.forEachIndexed { index, child ->
				val child = StubLogWaterAdditiveBinding.bind(child)
				if (child.description.editText?.text.isNullOrBlank() && child.amount.editText?.text.isNullOrBlank())
				{
					emptyCount++
					emptyIndex = index
				}
			}

			if (!additiveView.description.editText?.text.isNullOrBlank()
				&& !additiveView.amount.editText?.text.isNullOrBlank())
			{
				if (emptyCount < 1)
				{
					addAdditiveView(container)
					updateAdditiveTotals(bindings.waterAmount.editText?.text?.toDoubleOrNull() ?: 0.0)
				}
			}
			else if (emptyCount > 1 && emptyIndex < container.childViews.count())
			{
				container.removeViewAt(emptyIndex)
				log.additives.removeAt(emptyIndex)
			}

			// change focus if deleting
			if (text?.length == 0)
			{
				when (additiveView.root.findFocus())
				{
					additiveView.amount.editText -> additiveView.description.requestFocus()
					else -> {
						val currentViewIndex = container.indexOfChild(additiveView.root)
						if (currentViewIndex > 0)
						{
							val previousView = container.getChildAt(currentViewIndex - 1)
							StubLogWaterAdditiveBinding.bind(previousView).amount.requestFocus()
						}
					}
				}
			}
		}

		additiveView.amount.editText!!.doOnTextChanged(textChangeListener)
		additiveView.amount.editText!!.onFocusLoss {
			additive.amount = it.text.toDoubleOrNull() ?: 0.0
		}
		additiveView.amount.editText!!.doAfterTextChanged {
			updateAdditiveTotals(bindings.waterAmount.editText?.text?.toDoubleOrNull() ?: 0.0)
		}
		additiveView.description.editText!!.doOnTextChanged(textChangeListener)
		additiveView.description.editText!!.onFocusLoss {
			additive.description = it.text.toStringOrNull() ?: ""
		}
		container += additiveView.root
		return additiveView.root
	}

	override fun saveView(): Water
	{
		bindings.root.clearFocus()
		return log
	}

	override fun provideTitle(): String? = "Edit water log"
}
