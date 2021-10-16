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

class WaterLogView : LogView<FragmentActionLogWaterBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: Water

	constructor() : super()
	constructor(diary: Diary, log: Water) : super()
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): FragmentActionLogWaterBinding
		= FragmentActionLogWaterBinding.inflate(inflater, parent, false)

	override fun bindView(view: View): FragmentActionLogWaterBinding
		= FragmentActionLogWaterBinding.bind(view)

	override fun bind(view: FragmentActionLogWaterBinding)
	{
		view.waterPh.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.inPH = Water.PHUnit(it) }
		}

		view.waterRunoff.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.outPH = Water.PHUnit(it) }
		}

		view.waterAmount.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.amount = Volume(it, VolumeUnit.L) }
		}
		view.waterAmount.editText!!.doAfterTextChanged { text ->
			updateAdditiveTotals(view, text?.toDoubleOrNull() ?: 0.0)
		}

		view.waterTds.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.tds = Water.TdsUnit(it, TdsType.EC) }
		}
		view.waterTemp.editText!!.onFocusLoss {
			it.text.toDoubleOrNull()?.let { log.temperature = it }
		}

		view.waterPh.editText!!.text = log.inPH?.amount.asStringOrNull()?.asEditable()
		view.waterRunoff.editText!!.text = log.outPH?.amount.asStringOrNull()?.asEditable()
		view.waterAmount.editText!!.text = log.amount?.amount.asStringOrNull()?.asEditable()
		view.waterTds.editText!!.text = log.tds?.amount.asStringOrNull()?.asEditable()
		view.waterTemp.editText!!.text = log.temperature?.asStringOrNull()?.asEditable()

		addAdditiveView(view, view.additivesContainer)
	}

	private fun updateAdditiveTotals(view: FragmentActionLogWaterBinding, amount: Double)
	{
		view.additivesContainer.childViews.forEach { child ->
			val child = StubLogWaterAdditiveBinding.bind(child)
			val total = (child.amountEdit.text!!.toDoubleOrNull() ?: 0.0) * amount
			child.totals.text = "@${total.asStringOrNull()}ml"
			child.totals.isVisible = child.totals.text.isNotEmpty()
		}
	}

	private fun addAdditiveView(view: FragmentActionLogWaterBinding, container: ViewGroup): View
	{
		val additive = Water.Additive(amount = 0.0)
		log.additives += additive

		val additiveView = StubLogWaterAdditiveBinding.inflate(LayoutInflater.from(container.context), container, false)
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
					addAdditiveView(view, container)
					view.waterAmount.editText?.text?.toDoubleOrNull()?.let { updateAdditiveTotals(view, it) }
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
			view.waterAmount.editText?.text?.toDoubleOrNull()?.let { updateAdditiveTotals(view, it) }
		}
		additiveView.description.editText!!.doOnTextChanged(textChangeListener)
		additiveView.description.editText!!.onFocusLoss {
			additive.description = it.text.toStringOrNull() ?: ""
		}
		container += additiveView.root
		return additiveView.root
	}

	override fun save(view: FragmentActionLogWaterBinding): Log
	{
		view.root.clearFocus()

		// remove empty additives
		log.additives.removeAll { it.amount == 0.0 && it.description.isEmpty() }

		return log
	}

	override fun provideTitle(): String = "Edit water log"
}
