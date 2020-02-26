package me.anon.grow.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.plusAssign
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlinx.android.synthetic.main.action_buttons_stub.*
import kotlinx.android.synthetic.main.data_label_stub.view.*
import kotlinx.android.synthetic.main.statistics2_view.*
import me.anon.grow.R
import me.anon.lib.TdsUnit
import me.anon.lib.Unit
import me.anon.lib.ext.formatWhole
import me.anon.lib.helper.TimeHelper
import me.anon.model.*
import java.lang.Math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * // TODO: Add class description
 */
class StatisticsFragment2 : Fragment()
{
	companion object
	{
		@JvmStatic
		public fun newInstance(args: Bundle) = StatisticsFragment2().apply {
			this.arguments = args
		}
	}

	private lateinit var plant: Plant
	private val selectedTdsUnit by lazy { TdsUnit.getSelectedTdsUnit(activity!!) }
	private val selectedDeliveryUnit by lazy { Unit.getSelectedDeliveryUnit(activity!!) }
	private val selectedMeasurementUnit by lazy { Unit.getSelectedMeasurementUnit(activity!!) }
	private val checkedAdditives = setOf<String>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= inflater.inflate(R.layout.statistics2_view, container, false)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		(savedInstanceState ?: arguments)?.let {
			plant = it.getParcelable<Plant>("plant") as Plant
		}

		if (!::plant.isInitialized) return

		populateGeneralStats()
	}

	private fun populateGeneralStats()
	{
		open class template()
		open class header(var label: String) : template()
		open class data(label: String, val data: String) : header(label)

		val statTemplates = arrayListOf<template>()

		stats_container.removeAllViews()
		val plantStages = plant.calculateStageTime()
		plantStages.remove(PlantStage.HARVESTED)
		PlantStage.values().forEach { stage ->
			if (plantStages.containsKey(stage))
			{
				plantStages[stage]?.let { time ->
					statTemplates += data(
						label = "${getString(stage.printString)}:",
						data = "${TimeHelper.toDays(time).toInt()} ${resources.getQuantityString(R.plurals.time_day, TimeHelper.toDays(time).toInt())}"
					)
				}
			}
		}

		val aveStageWaters = LinkedHashMap<PlantStage, ArrayList<Long>>()
		aveStageWaters.putAll(plantStages.keys.map { it }.associateWith { arrayListOf<Long>() })
		val stageChanges = plant.getStages()
		stageChanges.toSortedMap(Comparator { first, second ->
			(stageChanges[first]?.date ?: 0).compareTo(stageChanges[second]?.date ?: 0)
		})

		val startDate = plant.plantDate
		var endDate = System.currentTimeMillis()
		var waterDifference = 0L
		var lastWater = 0L
		var totalWater = 0
		var totalWaterAmount = 0.0
		var totalFlush = 0

		plant.actions?.forEach { action ->
			when (action)
			{
				is StageChange -> {
					if (action.newStage == PlantStage.HARVESTED) endDate = action.date
				}

				is Water -> {
					if (lastWater != 0L) waterDifference += abs(action.date - lastWater)
					totalWater++
					totalWaterAmount += action.amount ?: 0.0
					lastWater = action.date

					// find the stage change where the date is older than the watering
					val stage = stageChanges.filterValues { it.date <= action.date }.toSortedMap().lastKey()
					aveStageWaters.getOrPut(stage, { arrayListOf<Long>() }).add(action.date)
				}

				is EmptyAction -> {
					if (action.action == Action.ActionName.FLUSH) totalFlush++
				}
			}
		}

		val days = ((endDate - startDate) / 1000.0) * 0.0000115741

		// total time
		statTemplates += data(
			label = getString(R.string.total_time_label),
			data = "${days.formatWhole()} ${resources.getQuantityString(R.plurals.time_day, days.toInt())}"
		)

		statTemplates += header("Water stats")

		// total waters
		statTemplates += data(
			label = getString(R.string.total_waters_label),
			data = "${totalWater.formatWhole()}"
		)

		// total flushes
		statTemplates += data(
			label = getString(R.string.total_flushes_label),
			data = "${totalFlush.formatWhole()}"
		)

		// total water amount
		statTemplates += data(
			label = getString(R.string.total_water_amount_label),
			data = "${Unit.ML.to(selectedDeliveryUnit, totalWaterAmount).formatWhole()} ${selectedDeliveryUnit.label}"
		)

		// average water amount
		statTemplates += data(
			label = getString(R.string.ave_water_amount_label),
			data = "${Unit.ML.to(selectedDeliveryUnit, (totalWaterAmount / totalWater.toDouble())).formatWhole()} ${selectedDeliveryUnit.label}"
		)

		// ave time between water
		statTemplates += data(
			label = getString(R.string.ave_time_between_water_label),
			data = (TimeHelper.toDays(waterDifference) / totalWater).let { d ->
				"${d.formatWhole()} ${resources.getQuantityString(R.plurals.time_day, ceil(d).toInt())}"
			}
		)

		// ave water time between stages
		aveStageWaters
			.toSortedMap(Comparator { first, second -> first.ordinal.compareTo(second.ordinal) })
			.forEach { (stage, dates) ->
				if (dates.isNotEmpty())
				{
					var dateDifference = dates.last() - dates.first()
					statTemplates += data(
						label = getString(R.string.ave_time_stage_label, stage.enString),
						data = (TimeHelper.toDays(dateDifference) / dates.size).let { d ->
							"${d.formatWhole()} ${resources.getQuantityString(R.plurals.time_day, ceil(d).toInt())}"
						}
					)
				}
			}

		statTemplates.forEach { template ->
			var dataView = when (template)
			{
				is data -> {
					LayoutInflater.from(activity).inflate(R.layout.data_label_stub, stats_container, false).also {
						it.label.text = template.label
						it.data.text = template.data
					}
				}

				is header -> {
					LayoutInflater.from(activity).inflate(R.layout.subtitle_stub, stats_container, false).also {
						(it as TextView).text = template.label
						it.setPadding(0, resources.getDimension(R.dimen.padding_16dp).toInt(), 0, 0)
					}
				}

				else -> null
			}

			dataView ?: return
			stats_container += dataView
		}

		// stage chart
		val entry = arrayListOf<BarEntry>()
		val labels = arrayOfNulls<String>(plantStages.size)
		val yVals = FloatArray(plantStages.size)

		val statsColours = resources.getStringArray(R.array.stats_colours).map {
			Color.parseColor(it)
		}

		var index = plantStages.size - 1
		for (plantStage in plantStages.keys)
		{
			yVals[index] = max(TimeHelper.toDays(plantStages[plantStage] ?: 0).toFloat(), 1f)
			labels[index--] = getString(plantStage.printString)
		}

		val stageEntries = arrayListOf<BarEntry>()
		stageEntries += BarEntry(0f, yVals, plantStages.keys.toList().asReversed())

		val stageData = BarDataSet(stageEntries, "")
		stageData.isHighlightEnabled = false
		stageData.stackLabels = labels
		stageData.setColors(statsColours)
		stageData.valueFormatter = object : ValueFormatter()
		{
			override fun getBarStackedLabel(value: Float, stackedEntry: BarEntry?): String
			{
				stackedEntry?.let {
					(it.data as? List<PlantStage>)?.let { stages ->
						val stageIndex = it.yVals.indexOf(value)
						return "${value.toInt()}${getString(stages[stageIndex].printString)[0].toLowerCase()}"
					}
				}

				return super.getBarStackedLabel(value, stackedEntry)
			}
		}

		val barData = BarData(stageData)

		stage_chart.data = barData
		stage_chart.setDrawGridBackground(false)
		stage_chart.description = null
		stage_chart.isScaleYEnabled = false
		stage_chart.setDrawBorders(false)
		stage_chart.setDrawValueAboveBar(false)

		stage_chart.axisLeft.setDrawGridLines(false)
		stage_chart.axisLeft.axisMinimum = 0f
		stage_chart.axisLeft.textColor = 0xffffffff.toInt()
		stage_chart.axisLeft.valueFormatter = object : ValueFormatter()
		{
			override fun getAxisLabel(value: Float, axis: AxisBase?): String
			{
				return "${value.toInt()}${getString(R.string.day_abbr)}"
			}
		}

		stage_chart.axisRight.setDrawLabels(false)
		stage_chart.axisRight.setDrawGridLines(false)

		stage_chart.xAxis.setDrawGridLines(false)
		stage_chart.xAxis.setDrawAxisLine(false)
		stage_chart.xAxis.setDrawLabels(false)

		stage_chart.legend.textColor = 0xffffffff.toInt()
		stage_chart.legend.isWordWrapEnabled = true
	}
}
