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
import me.anon.grow.R
import me.anon.grow.databinding.ScheduleDateDetailsViewBinding
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
	private lateinit var binding: ScheduleDateDetailsViewBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= ScheduleDateDetailsViewBinding.inflate(inflater, container, false).let {
		binding = it
		it.root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		(savedInstanceState ?: arguments).let {
			date = it?.get("schedule_date") as FeedingScheduleDate?
			additives = (it?.get("additives") as ArrayList<Additive>?) ?: date?.additives ?: arrayListOf<Additive>()
		}

		binding.toStage.adapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, PlantStage.values().map { getString(it.printString) }.toTypedArray())
		binding.fromStage.adapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, PlantStage.values().map { getString(it.printString) }.toTypedArray())

		binding.fromStage.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
		{
			override fun onNothingSelected(parent: AdapterView<*>?){}

			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
			{
				binding.fromStage.tag = PlantStage.values()[position]
				if (binding.toStage.selectedItemPosition < position)
				{
					binding.toStage.setSelection(position)
				}
			}
		}

		binding.toStage.onItemSelectedListener = object: AdapterView.OnItemSelectedListener
		{
			override fun onNothingSelected(parent: AdapterView<*>?){}

			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
			{
				binding.toStage.tag = PlantStage.values()[position]
				if (binding.fromStage.selectedItemPosition > position)
				{
					binding.fromStage.setSelection(position)
				}
			}
		}

		binding.fromDate.addTextChangedListener(object: TextWatcher
		{
			override fun afterTextChanged(s: Editable?){}
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int){}

			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
			{
				if (binding.toDate.text.isEmpty()) binding.toDate.setText(s)
			}
		})

		date?.let {
			binding.fromStage.setSelection(it.stageRange[0].ordinal)
			binding.fromStage.tag = it.stageRange[0]
			binding.toStage.setSelection(it.stageRange[1].ordinal)
			binding.toStage.tag = it.stageRange[1]
			binding.fromDate.setText(it.dateRange[0].toString())
			binding.toDate.setText(it.dateRange[1].toString())

			this@ScheduleDateDetailsFragment.additives = additives
		}

		populateAdditives()

		binding.fabComplete.setOnClickListener {
			if (binding.fromDate.text.isEmpty())
			{
				binding.fromDate.error = "From date is required"
				return@setOnClickListener
			}

			val fromDate = binding.fromDate.text.toString().toSafeInt()
			val toDate = if (binding.toDate.text.isEmpty()) fromDate else binding.toDate.text.toString().toSafeInt()
			val fromStage = binding.fromStage.tag as PlantStage?
			val toStage = binding.toStage.tag as PlantStage?

			if (toDate < fromDate && fromStage?.ordinal ?: -1 == toStage?.ordinal ?: -1)
			{
				binding.toDate.error = "Date can not be before from date"
				return@setOnClickListener
			}

			val date = when (date)
			{
				null -> {
					FeedingScheduleDate(
						dateRange = arrayOf(fromDate, if (binding.toDate.text.isEmpty()) fromDate else toDate),
						stageRange = arrayOf(fromStage!!, toStage!!),
						additives = additives
					)
				}
				else -> {
					date!!.apply {
						dateRange = arrayOf(fromDate, if (binding.toDate.text.isEmpty()) fromDate else toDate)
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

			val additiveStub = LayoutInflater.from(activity).inflate(R.layout.additive_stub, binding.additiveContainer, false)
			(additiveStub as TextView).text = "${additive.description}   -   $amountStr${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"

			additiveStub.setTag(additive)
			additiveStub.setOnClickListener { view -> onNewAdditiveClick(view) }
			binding.additiveContainer.addView(additiveStub, binding.additiveContainer.childCount - 1)
		}

		binding.newAdditive.setOnClickListener { onNewAdditiveClick(it) }
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

				val additiveStub = LayoutInflater.from(activity).inflate(R.layout.additive_stub, binding.additiveContainer, false)
				(additiveStub as TextView).text = "${additive.description}   -   $amountStr${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"

				if (currentTag == null)
				{
					if (!additives.contains(additive))
					{
						additives.add(additive)

						additiveStub.setTag(additive)
						additiveStub.setOnClickListener { view -> onNewAdditiveClick(view) }
						binding.additiveContainer.addView(additiveStub, binding.additiveContainer.childCount - 1)
					}
				}
				else
				{
					for (childIndex in 0 until binding.additiveContainer.childCount)
					{
						val tag = binding.additiveContainer.getChildAt(childIndex).tag

						if (tag === currentTag)
						{
							converted = Unit.ML.to(selectedMeasurementUnit, additive.amount!!)
							amountStr = if (converted == floor(converted)) converted.toInt().toString() else converted.toString()

							additives[childIndex] = additive

							(binding.additiveContainer.getChildAt(childIndex) as TextView).text = "${additive.description}   -   $amountStr${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"
							binding.additiveContainer.getChildAt(childIndex).tag = additive

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

				for (childIndex in 0 until binding.additiveContainer.childCount)
				{
					val tag = binding.additiveContainer.getChildAt(childIndex).tag

					if (tag === additive)
					{
						binding.additiveContainer.removeViewAt(childIndex)
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
