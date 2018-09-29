package me.anon.grow.fragment

import android.app.Fragment
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.schedule_date_details_view.*
import me.anon.grow.R
import me.anon.lib.Unit
import me.anon.lib.manager.ScheduleManager
import me.anon.model.Additive
import me.anon.model.FeedingScheduleDate
import me.anon.model.PlantStage

/**
 * // TODO: Add class description
 */
class ScheduleDateDetailsFragment : Fragment()
{
	companion object
	{
		fun newInstance(scheduleIndex: Int = -1, dateIndex: Int = -1): ScheduleDateDetailsFragment
		{
			return ScheduleDateDetailsFragment().apply {
				this.scheduleIndex = scheduleIndex
				this.dateIndex = dateIndex
			}
		}
	}

	private var scheduleIndex: Int = -1
	private var dateIndex: Int = -1
	private var additives: ArrayList<Additive> = arrayListOf()
	private val selectedMeasurementUnit: Unit by lazy { Unit.getSelectedMeasurementUnit(activity); }
	private val selectedDeliveryUnit: Unit by lazy { Unit.getSelectedDeliveryUnit(activity); }

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View
		= inflater?.inflate(R.layout.schedule_date_details_view, container, false) ?: View(activity)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		to_stage.adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, PlantStage.values().map { it.printString }.toTypedArray())
		from_stage.adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, PlantStage.values().map { it.printString }.toTypedArray())

		from_stage.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
		{
			override fun onNothingSelected(parent: AdapterView<*>?){}

			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
			{
				if (to_stage.selectedItemPosition < position)
				{
					to_stage.setSelection(position)
				}
			}
		}

		to_stage.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
		{
			override fun onNothingSelected(parent: AdapterView<*>?){}

			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
			{
				if (from_stage.selectedItemPosition > position)
				{
					from_stage.setSelection(position)
				}
			}
		}

		if (dateIndex > -1)
		{
			ScheduleManager.instance.schedules[scheduleIndex].schedules[dateIndex].apply {
				to_stage.setSelection(this.stageRange[0].ordinal)
				from_stage.setSelection(this.stageRange[1].ordinal)
				to_date.setText(this.dateRange[0].toString())
				from_date.setText(this.dateRange[1].toString())
			}
		}

		populateAdditives()

		fab_complete.setOnClickListener {
			val fromDate = from_date.text.toString().toInt()
			val toDate = to_date.text.toString().toInt()
			val fromStage = PlantStage.valueOfPrintString(to_stage.selectedItem as String)!!
			val toStage = PlantStage.valueOfPrintString(from_stage.selectedItem as String)!!

			if (fromDate > toDate && fromStage.ordinal ?: -1 <= toStage.ordinal ?: -1)
			{
				to_date.error = "Date can not be before from date"
				return@setOnClickListener
			}

			when (dateIndex)
			{
				-1 -> {
					val date: FeedingScheduleDate = FeedingScheduleDate(
						dateRange = arrayOf<Int>(fromDate, if (to_date.text.isEmpty()) fromDate else toDate),
						stageRange = arrayOf<PlantStage>(fromStage, toStage),
						additives = additives
					)

					ScheduleManager.instance.schedules[scheduleIndex].schedules.add(date)
				}
				else -> {
					ScheduleManager.instance.schedules[scheduleIndex].schedules[dateIndex].apply {
						this.dateRange = arrayOf<Int>(fromDate, if (to_date.text.isEmpty()) fromDate else toDate)
						this.stageRange = arrayOf<PlantStage>(fromStage, toStage)
						this.additives = this@ScheduleDateDetailsFragment.additives
					}
				}
			}

			activity.finish()
		}
	}

	private fun populateAdditives()
	{
		for (additive in additives)
		{
			if (additive.amount == null) continue

			val converted = Unit.ML.to(selectedMeasurementUnit, additive.amount)
			val amountStr = if (converted == Math.floor(converted)) converted.toInt().toString() else converted.toString()

			val additiveStub = LayoutInflater.from(activity).inflate(R.layout.additive_stub, additive_container, false)
			(additiveStub as TextView).text = "${additive.description}   -   $amountStr${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"

			additiveStub.setTag(additive)
			additiveStub.setOnClickListener { view -> onNewAdditiveClick(view) }
			additive_container.addView(additiveStub, additive_container.childCount - 1)
		}

		new_additive.setOnClickListener { onNewAdditiveClick(it) }
	}

	private fun onNewAdditiveClick(view: View)
	{
		val currentTag = view.tag
		val fm = fragmentManager
		val addAdditiveDialogFragment = AddAdditiveDialogFragment(if (view.tag is Additive) view.tag as Additive else null)
		addAdditiveDialogFragment.setOnAdditiveSelectedListener(object : AddAdditiveDialogFragment.OnAdditiveSelectedListener
		{
			override fun onAdditiveSelected(additive: Additive)
			{
				if (TextUtils.isEmpty(additive.description))
				{
					return
				}

				var converted = Unit.ML.to(selectedMeasurementUnit, additive.amount!!)
				var amountStr = if (converted == Math.floor(converted)) converted.toInt().toString() else converted.toString()

				val additiveStub = LayoutInflater.from(activity).inflate(R.layout.additive_stub, additive_container, false)
				(additiveStub as TextView).text = "${additive.description}   -   $amountStr${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"

				if (currentTag == null)
				{
					if (!additives.contains(additive))
					{
						additives.add(additive)

						additiveStub.setTag(additive)
						additiveStub.setOnClickListener { view -> onNewAdditiveClick(view) }
						additive_container.addView(additiveStub, additive_container.childCount - 1)
					}
				}
				else
				{
					for (childIndex in 0 until additive_container.childCount)
					{
						val tag = additive_container.getChildAt(childIndex).tag

						if (tag === currentTag)
						{
							converted = Unit.ML.to(selectedMeasurementUnit, additive.amount!!)
							amountStr = if (converted == Math.floor(converted)) converted.toInt().toString() else converted.toString()

							additives[childIndex] = additive

							(additive_container.getChildAt(childIndex) as TextView).text = "${additive.description}   -   $amountStr${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"
							additive_container.getChildAt(childIndex).tag = additive

							break
						}
					}
				}

				(additiveStub.parent as View).requestFocus()
				(additiveStub.parent as View).requestFocusFromTouch()
			}

			override fun onAdditiveDeleteRequested(additive: Additive)
			{
				if (additives.contains(additive))
				{
					additives.remove(additive)
				}

				for (childIndex in 0 until additive_container.childCount)
				{
					val tag = additive_container.getChildAt(childIndex).tag

					if (tag === additive)
					{
						additive_container.removeViewAt(childIndex)
						break
					}
				}

				additive_container.getChildAt(additive_container.childCount - 1).requestFocus()
				additive_container.getChildAt(additive_container.childCount - 1).requestFocusFromTouch()
			}
		})

		addAdditiveDialogFragment.show(fm, "fragment_add_additive")
	}
}
