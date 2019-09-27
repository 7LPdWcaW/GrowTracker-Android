package me.anon.grow.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.schedule_date_details_view.*
import me.anon.grow.R
import me.anon.lib.Unit
import me.anon.lib.ext.toSafeInt
import me.anon.model.Additive
import me.anon.model.FeedingScheduleDate
import me.anon.model.PlantStage
import kotlin.math.floor

class ScheduleDateDetailsFragment : Fragment()
{
	companion object
	{
		fun newInstance(bundle: Bundle): ScheduleDateDetailsFragment
		{
			return ScheduleDateDetailsFragment().apply {
				arguments = bundle
			}
		}
	}

	private var date: FeedingScheduleDate? = null
	private var additives: ArrayList<Additive> = arrayListOf()
	private val selectedMeasurementUnit: Unit by lazy { Unit.getSelectedMeasurementUnit(activity); }
	private val selectedDeliveryUnit: Unit by lazy { Unit.getSelectedDeliveryUnit(activity); }

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= inflater.inflate(R.layout.schedule_date_details_view, container, false) ?: View(activity)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		(savedInstanceState ?: arguments).let {
			date = it?.get("schedule_date") as FeedingScheduleDate?
			additives = (it?.get("additives") as ArrayList<Additive>?) ?: date?.additives ?: arrayListOf<Additive>()
		}

		to_stage.adapter = ArrayAdapter<String>(activity!!, android.R.layout.simple_list_item_1, PlantStage.values().map { getString(it.printString) }.toTypedArray())
		from_stage.adapter = ArrayAdapter<String>(activity!!, android.R.layout.simple_list_item_1, PlantStage.values().map { getString(it.printString) }.toTypedArray())

		from_stage.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
		{
			override fun onNothingSelected(parent: AdapterView<*>?){}

			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
			{
				from_stage.tag = PlantStage.values()[position]
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
				to_stage.tag = PlantStage.values()[position]
				if (from_stage.selectedItemPosition > position)
				{
					from_stage.setSelection(position)
				}
			}
		}

		from_date.addTextChangedListener(object: TextWatcher
		{
			override fun afterTextChanged(s: Editable?){}
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){}

			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
			{
				if (to_date.text.isEmpty()) to_date.setText(s)
			}
		})

		date?.let {
			from_stage.setSelection(it.stageRange[0].ordinal)
			from_stage.tag = it.stageRange[0]
			to_stage.setSelection(it.stageRange[1].ordinal)
			to_stage.tag = it.stageRange[1]
			from_date.setText(it.dateRange[0].toString())
			to_date.setText(it.dateRange[1].toString())

			this@ScheduleDateDetailsFragment.additives = additives
		}

		populateAdditives()

		fab_complete.setOnClickListener {
			if (from_date.text.isEmpty())
			{
				from_date.error = "From date is required"
				return@setOnClickListener
			}

			val fromDate = from_date.text.toString().toSafeInt()
			val toDate = if (to_date.text.isEmpty()) fromDate else to_date.text.toString().toSafeInt()
			val fromStage = from_stage.tag as PlantStage?
			val toStage = to_stage.tag as PlantStage?

			if (toDate < fromDate && fromStage?.ordinal ?: -1 == toStage?.ordinal ?: -1)
			{
				to_date.error = "Date can not be before from date"
				return@setOnClickListener
			}

			val date = when (date)
			{
				null -> {
					FeedingScheduleDate(
						dateRange = arrayOf(fromDate, if (to_date.text.isEmpty()) fromDate else toDate),
						stageRange = arrayOf(fromStage!!, toStage!!),
						additives = additives
					)
				}
				else -> {
					date!!.apply {
						dateRange = arrayOf(fromDate, if (to_date.text.isEmpty()) fromDate else toDate)
						stageRange = arrayOf(fromStage!!, toStage!!)
						additives = this@ScheduleDateDetailsFragment.additives
					}
				}
			}

			activity?.setResult(Activity.RESULT_OK, Intent().also {
				it.putExtra("schedule_date", date)
			})
			activity?.finish()
		}
	}

	override fun onSaveInstanceState(outState: Bundle)
	{
		outState.putParcelable("schedule_date", date)
		outState.putParcelableArrayList("additives", additives)
		super.onSaveInstanceState(outState)
	}

	private fun populateAdditives()
	{
		for (additive in additives)
		{
			if (additive.amount == null) continue

			val converted = Unit.ML.to(selectedMeasurementUnit, additive.amount!!)
			val amountStr = if (converted == floor(converted)) converted.toInt().toString() else converted.toString()

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
		val currentFocus = activity?.currentFocus
		val currentTag = view.tag
		val fm = childFragmentManager
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
				var amountStr = if (converted == floor(converted)) converted.toInt().toString() else converted.toString()

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
							amountStr = if (converted == floor(converted)) converted.toInt().toString() else converted.toString()

							additives[childIndex] = additive

							(additive_container.getChildAt(childIndex) as TextView).text = "${additive.description}   -   $amountStr${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"
							additive_container.getChildAt(childIndex).tag = additive

							break
						}
					}
				}

				currentFocus?.let {
					it.requestFocus()
					it.requestFocusFromTouch()
				}
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

				currentFocus?.let {
					it.requestFocus()
					it.requestFocusFromTouch()
				}
			}
		})

		addAdditiveDialogFragment.show(fm, "fragment_add_additive")
	}
}
