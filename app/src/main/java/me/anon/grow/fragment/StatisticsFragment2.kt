package me.anon.grow.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.ColorUtils
import androidx.core.view.plusAssign
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.data_label_stub.view.*
import kotlinx.android.synthetic.main.statistics2_view.*
import me.anon.grow.R
import me.anon.lib.TdsUnit
import me.anon.lib.Unit
import me.anon.lib.ext.*
import me.anon.lib.helper.StatsHelper.formatter
import me.anon.lib.helper.TimeHelper
import me.anon.model.*
import java.lang.Math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * // TODO: Add class description
 */
class StatisticsFragment2 : Fragment()
{
	class StageDate(var day: Int, var total: Int, var stage: PlantStage)
	class StatWrapper(var min: Double? = null, var max: Double? = null, var average: Double? = null)

	open class template()
	open class header(var label: String) : template()
	open class data(label: String, val data: String) : header(label)

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
	private val statsColours by lazy {
		resources.getStringArray(R.array.stats_colours).map {
			Color.parseColor(it)
		}
	}

	// stat variables
	val stageChanges by lazy {
		plant.getStages().also {
			it.toSortedMap(Comparator { first, second ->
				(it[first]?.date ?: 0).compareTo(it[second]?.date ?: 0)
			})
		}
	}

	val plantStages by lazy {
		plant.calculateStageTime().also {
			it.remove(PlantStage.HARVESTED)
		}
	}

	val aveStageWaters by lazy {
		LinkedHashMap<PlantStage, ArrayList<Long>>().also { waters ->
			waters.putAll(plantStages.keys.map { it }.associateWith { arrayListOf<Long>() })
		}
	}

	val additives = hashMapOf<Water, ArrayList<Additive>>()
	val waterDates = arrayListOf<StageDate>()
	val additiveValues = hashMapOf<String, ArrayList<Entry>>()
	val additiveTotalValues = hashMapOf<String, ArrayList<Entry>>()

	val phValues = arrayListOf<Entry>()
	val phStats = StatWrapper()

	val runoffValues = arrayListOf<Entry>()
	val runoffStats = StatWrapper()

	var endDate = System.currentTimeMillis()
	var waterDifference = 0L
	var lastWater = 0L
	var totalWater = 0
	var totalWaterAmount = 0.0
	var totalFlush = 0

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
		= inflater.inflate(R.layout.statistics2_view, container, false)

	override fun onActivityCreated(savedInstanceState: Bundle?)
	{
		super.onActivityCreated(savedInstanceState)

		(savedInstanceState ?: arguments)?.let {
			plant = it.getParcelable<Plant>("plant") as Plant
		}

		if (!::plant.isInitialized) return

		calculateStats()
		populateGeneralStats()
		populateAdditiveStats()
		populatePhStats()
	}

	private fun calculateStats()
	{
		val tempPhValues = arrayListOf<Double>()
		val tempRunoffValues = arrayListOf<Double>()
		var waterIndex = 0
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
					val sortedStageChange = stageChanges.filterValues { it.date <= action.date }.toSortedMap()
					val stage = sortedStageChange.lastKey()
					val stageChangeDate = sortedStageChange[stage]?.date ?: 0
					val waterDate = action.date
					val stageLength = (waterDate - stageChangeDate).toDays().toInt()
					val totalDate = TimeHelper.toDays(action.date - plant.plantDate).toInt()
					waterDates.add(StageDate(stageLength, totalDate, stage))
					aveStageWaters.getOrPut(stage, { arrayListOf() }).add(action.date)

					// pH stats
					action.ph?.let {
						tempPhValues += it
						phStats.max = max(phStats.max ?: Double.MIN_VALUE, it)
						phStats.min = min(phStats.min ?: Double.MAX_VALUE, it)

						phValues += Entry(waterIndex.toFloat(), it.toFloat())
					}

					// runoff stats
					action.runoff?.let {
						tempRunoffValues += it
						runoffStats.max = max(runoffStats.max ?: Double.MIN_VALUE, it)
						runoffStats.min = min(runoffStats.min ?: Double.MAX_VALUE, it)

						runoffValues += Entry(waterIndex.toFloat(), it.toFloat())
					}

					// add additives to pre calculated list
					action.additives.forEach { additive ->
						if (additive.description != null)
						{
							additive.amount?.let { amount ->
								with (additiveValues) {
									val amount = Unit.ML.to(selectedMeasurementUnit, amount)
									val entry = Entry(waterIndex.toFloat(), amount.toFloat())
									getOrPut(additive.description!!, { arrayListOf() }).add(entry)
								}

								with (additiveTotalValues) {
									val totalDelivery = Unit.ML.to(selectedDeliveryUnit, action.amount ?: 1000.0)
									val additiveAmount = Unit.ML.to(selectedMeasurementUnit, amount)

									val entry = Entry(waterIndex.toFloat(), Unit.toTwoDecimalPlaces(additiveAmount * totalDelivery).toFloat())
									getOrPut(additive.description!!, { arrayListOf() }).add(entry)
								}
							}
						}
					}

					waterIndex++
					additives.getOrPut(action) { arrayListOf() }.addAll(action.additives)
				}

				is EmptyAction -> {
					if (action.action == Action.ActionName.FLUSH) totalFlush++
				}
			}
		}

		phStats.average = if (tempPhValues.isNotEmpty()) tempPhValues.average() else null
		runoffStats.average = if (tempRunoffValues.isNotEmpty()) tempRunoffValues.average() else null
	}

	private fun populateGeneralStats()
	{
		val statTemplates = arrayListOf<template>()

		stats_container.removeAllViews()

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

		val startDate = plant.plantDate
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

		renderStats(stats_container, statTemplates)

		// stage chart
		val labels = arrayOfNulls<String>(plantStages.size)
		val yVals = FloatArray(plantStages.size)

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
		stageData.valueTypeface = Typeface.DEFAULT_BOLD
		stageData.valueTextSize = 10f
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
		stage_chart.axisLeft.textColor = R.attr.colorOnSurface.resolveColor(context!!)
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

		stage_chart.legend.textColor = R.attr.colorOnSurface.resolveColor(context!!).toInt()
		stage_chart.legend.isWordWrapEnabled = true
	}

	private fun populateAdditiveStats()
	{
		class additiveStat(
			var total: Double = 0.0,
			var totalAdjusted: Double = 0.0,
			var count: Int = 0,
			var min: Double = Double.NaN,
			var max: Double = Double.NaN
		)

		val selectedAdditives = arrayListOf<String>()
		val additiveStats = LinkedHashMap<String, additiveStat>()
		var totalMax = Double.MIN_VALUE

		fun displayStats()
		{
			additives_stats_container.removeAllViews()

			selectedAdditives.forEach { name ->
				additiveStats[name]?.let { stat ->
					val stats = arrayListOf<template>()
					stats += header(getString(R.string.additive_stat_header, name))
					stats += data(
						label = getString(R.string.min),
						data = "${Unit.ML.to(selectedMeasurementUnit, stat.min).formatWhole()} ${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"
					)
					stats += data(
						label = getString(R.string.max),
						data = "${Unit.ML.to(selectedMeasurementUnit, stat.max).formatWhole()} ${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"
					)
					stats += data(
						label = getString(R.string.additive_average_usage_label),
						data = "${Unit.ML.to(selectedMeasurementUnit, stat.total / stat.count.toDouble()).formatWhole()} ${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"
					)
					stats += data(
						label = getString(R.string.additive_usage_count_label),
						data = "${stat.count}"
					)
					stats += data(
						label = getString(R.string.additive_total_usage_label),
						data = "${Unit.ML.to(selectedMeasurementUnit, stat.totalAdjusted).formatWhole()} ${selectedMeasurementUnit.label}"
					)

					renderStats(additives_stats_container, stats)
				}
			}
		}

		fun displayConcentrationChart()
		{
			val barSets = arrayListOf<IBarDataSet>()
			val dataSets = arrayListOf<ILineDataSet>()
			var index = 0
			additiveValues.toSortedMap().let {
				it.forEach { (k, v) ->
					if (selectedAdditives.contains(k))
					{
						dataSets += LineDataSet(v, k).apply {
							color = statsColours[index]
							fillColor = color
							setCircleColor(color)
							styleDataset(context!!, this, color)
						}
					}

					index++
					if (index >= statsColours.size) index = 0
				}
			}

			val stageEntries = plantStages.keys.toList().asReversed()
			waterDates.forEachIndexed { additiveIndex, date ->
				barSets += BarDataSet(arrayListOf(BarEntry(additiveIndex.toFloat(), totalMax.toFloat())), null).apply {
					color = ColorUtils.setAlphaComponent(statsColours[stageEntries.indexOf(date.stage) % statsColours.size], 127)
				}
			}

			val lineData = LineData(dataSets)
			val barData = BarData(barSets).apply {
				setDrawValues(false)
				this.isHighlightEnabled = false
				this.barWidth = 1.0f
				this.groupBars(0f, 0f, 0f)
			}

			val data = CombinedData()
			//data.setData(barData)
			data.setData(lineData)
			additives_concentration_chart.data = data
			additives_concentration_chart.notifyDataSetChanged()
			additives_concentration_chart.invalidate()
		}

		fun displayTotalsChart()
		{
			val pieData = arrayListOf<PieEntry>()
			val colors = arrayListOf<Int>()
			additiveTotalValues.toSortedMap().let { values ->
				var index = 0

				values.forEach { (k, v) ->
					if (selectedAdditives.contains(k))
					{
						var total = 0.0
						v.forEach { entry ->
							total += entry.y
						}

						pieData += PieEntry(total.toFloat()).apply {
							colors += statsColours[index]
						}
					}

					index++
					if (index >= statsColours.size) index = 0
				}
			}

			additives_count_chart.data = PieData(PieDataSet(pieData, "").apply {
				this.colors = colors
				this.valueTextSize = 12f
				this.valueFormatter = object : ValueFormatter()
				{
					override fun getFormattedValue(value: Float): String
					{
						return "${value.formatWhole()}${selectedMeasurementUnit.label}"
					}
				}
			})
			additives_count_chart.notifyDataSetChanged()
			additives_count_chart.invalidate()
		}

		fun displayOvertimeChart()
		{
			val barSets = arrayListOf<IBarDataSet>()
			val dataSets = arrayListOf<ILineDataSet>()
			var index = 0
			val newValues = sortedMapOf<String, ArrayList<Entry>>()

			additiveTotalValues.toSortedMap().let {
				it.forEach { (key, entries) ->
					if (selectedAdditives.contains(key))
					{
						val newEntries = arrayListOf<Entry>()
						var lastEntry: Entry? = null
						entries.forEach { entry ->
							val newEntry = Entry(entry.x, entry.y + (lastEntry?.y ?: 0.0f))
							newEntries.add(newEntry)
							lastEntry = newEntry
						}

						newValues[key] = newEntries

						dataSets += LineDataSet(newValues[key], key).apply {
							color = statsColours[index]
							fillColor = color
							setCircleColor(color)
							styleDataset(context!!, this, color)
						}
					}

					index++
					if (index >= statsColours.size) index = 0
				}
			}

			val stageEntries = plantStages.keys.toList().asReversed()
			waterDates.forEachIndexed { additiveIndex, date ->
				barSets += BarDataSet(arrayListOf(BarEntry(additiveIndex.toFloat(), totalMax.toFloat())), null).apply {
					color = ColorUtils.setAlphaComponent(statsColours[stageEntries.indexOf(date.stage) % statsColours.size], 127)
				}
			}

			val lineData = LineData(dataSets)
			val barData = BarData(barSets).apply {
				setDrawValues(false)
				this.isHighlightEnabled = false
				this.barWidth = 1.0f
				this.groupBars(0f, 0f, 0f)
			}

			val data = CombinedData()
			//data.setData(barData)
			data.setData(lineData)
			additives_overtime_chart.data = data
			additives_overtime_chart.notifyDataSetChanged()
			additives_overtime_chart.invalidate()
		}

		fun refreshCharts()
		{
			displayConcentrationChart()
			displayTotalsChart()
			displayOvertimeChart()
			displayStats()
		}

		additives.keys.forEach { water ->
			additives[water]?.sortedBy { it.description }?.forEach { additive ->
				additive.description?.let { key ->
					additiveStats.getOrPut(key, { additiveStat() }).apply {
						total += additive.amount ?: 0.0
						min = min(min.isNaN() T Double.MAX_VALUE ?: min, additive.amount ?: 0.0)
						max = max(max.isNaN() T Double.MIN_VALUE ?: max, additive.amount ?: 0.0)

						additiveTotalValues[key]?.let { totalValues ->
							var total = 0.0
							totalValues.forEach { entry ->
								total += entry.y
							}

							totalAdjusted = total
						}

						count++
						totalMax = max(totalMax, max)
					}
				}
			}
		}

		additiveStats.forEach { (k, v) ->
			val chip = LayoutInflater.from(context!!).inflate(R.layout.filter_chip_stub, additive_chips_container, false) as Chip
			chip.text = k
			chip.isChecked = true
			chip.setOnCheckedChangeListener { buttonView, isChecked ->
				if (isChecked) selectedAdditives += k
				else selectedAdditives -= k

				refreshCharts()
			}

			selectedAdditives += k
			additive_chips_container += chip
		}

		val entries = arrayListOf<LegendEntry>()
		additiveValues.toSortedMap().let {
			var index = 0
			it.forEach { (k, v) ->
				if (selectedAdditives.contains(k))
				{
					entries.add(LegendEntry().apply {
						label = k
						formColor = statsColours[index]
					})
				}

				index++
				if (index >= statsColours.size) index = 0
			}
		}

		with (additives_concentration_chart) {
			drawOrder = arrayOf(
				DrawOrder.BAR, DrawOrder.LINE
			)
			setVisibleYRangeMaximum(totalMax.toFloat(), YAxis.AxisDependency.LEFT)
			setDrawGridBackground(false)
			description = null
			isScaleYEnabled = false
			setDrawBorders(false)

			axisLeft.granularity = 1f
			axisLeft.setDrawGridLines(false)
			axisLeft.textColor = R.attr.colorOnSurface.resolveColor(context!!)
			axisLeft.valueFormatter = object : ValueFormatter()
			{
				override fun getAxisLabel(value: Float, axis: AxisBase?): String
				{
					return "${value.formatWhole()}${selectedMeasurementUnit.label}/${selectedDeliveryUnit.label}"
				}
			}

			axisRight.setDrawLabels(false)
			axisRight.setDrawGridLines(false)
			axisRight.setDrawAxisLine(false)

			xAxis.setDrawGridLines(false)
			xAxis.granularity = 1f
			xAxis.position = XAxis.XAxisPosition.BOTTOM
			xAxis.yOffset = 10f
			xAxis.textColor = R.attr.colorOnSurface.resolveColor(context!!)
			xAxis.valueFormatter = object : ValueFormatter()
			{
				override fun getAxisLabel(value: Float, axis: AxisBase?): String
				{
					return waterDates.getOrNull(value.toInt())?.transform {
						"${total}/${day}${getString(stage.printString).toLowerCase()[0]}"
					} ?: ""
				}
			}

			legend.setCustom(entries)
			legend.form = Legend.LegendForm.CIRCLE
			legend.textColor = R.attr.colorOnSurface.resolveColor(context!!)
			legend.isWordWrapEnabled = true
			legend.yOffset = 10f
			legend.xOffset = 10f
		}

		with (additives_overtime_chart) {
			drawOrder = arrayOf(
				DrawOrder.BAR, DrawOrder.LINE
			)
			setVisibleYRangeMaximum(totalMax.toFloat(), YAxis.AxisDependency.LEFT)
			setDrawGridBackground(false)
			description = null
			isScaleYEnabled = false
			setDrawBorders(false)

			axisLeft.granularity = 1f
			axisLeft.setDrawGridLines(false)
			axisLeft.textColor = R.attr.colorOnSurface.resolveColor(context!!)
			axisLeft.valueFormatter = object : ValueFormatter()
			{
				override fun getAxisLabel(value: Float, axis: AxisBase?): String
				{
					return "${value.formatWhole()}${selectedMeasurementUnit.label}"
				}
			}

			axisRight.setDrawLabels(false)
			axisRight.setDrawGridLines(false)
			axisRight.setDrawAxisLine(false)

			xAxis.setDrawGridLines(false)
			xAxis.granularity = 1f
			xAxis.position = XAxis.XAxisPosition.BOTTOM
			xAxis.yOffset = 10f
			xAxis.textColor = R.attr.colorOnSurface.resolveColor(context!!)
			xAxis.valueFormatter = object : ValueFormatter()
			{
				override fun getAxisLabel(value: Float, axis: AxisBase?): String
				{
					return waterDates.getOrNull(value.toInt())?.transform {
						"${total}/${day}${getString(stage.printString).toLowerCase()[0]}"
					} ?: ""
				}
			}

			legend.setCustom(entries)
			legend.form = Legend.LegendForm.CIRCLE
			legend.textColor = R.attr.colorOnSurface.resolveColor(context!!)
			legend.isWordWrapEnabled = true
			legend.yOffset = 10f
			legend.xOffset = 10f
		}

		with (additives_count_chart) {
			description = null
			setHoleColor(0x00ffffff)
			legend.setCustom(entries)
			legend.form = Legend.LegendForm.CIRCLE
			legend.textColor = R.attr.colorOnSurface.resolveColor(context!!)
			legend.isWordWrapEnabled = true
		}

		refreshCharts()
	}

	private fun populatePhStats()
	{
		val INPUT_PH = R.string.stat_input_ph
		val RUNOFF_PH = R.string.stat_runoff_ph
		val AVERAGE_PH = R.string.stat_average_ph
		val selectedModes = arrayListOf<Int>()

		fun refreshCharts()
		{
			val sets = arrayListOf<ILineDataSet>()

			if (INPUT_PH in selectedModes)
			{
				sets += LineDataSet(phValues, getString(R.string.stat_input_ph)).apply {
					color = statsColours[0]
					fillColor = color
					setCircleColor(color)
					styleDataset(context!!, this, color)
				}
			}

			if (RUNOFF_PH in selectedModes)
			{
				sets += LineDataSet(runoffValues, getString(R.string.stat_runoff_ph)).apply {
					color = statsColours[1]
					fillColor = color
					setCircleColor(color)
					styleDataset(context!!, this, color)
				}
			}

			if (AVERAGE_PH in selectedModes)
			{
				val averageEntries = arrayListOf<Entry>()
				phValues.foldIndexed(0.0f) { index, acc, entry ->
					if (acc > 0) averageEntries += Entry(index.toFloat(), acc / index)
					entry.y + acc
				}
				sets += LineDataSet(averageEntries, getString(R.string.stat_average_ph)).apply {
					color = ColorUtils.blendARGB(statsColours[0], 0xffffffff.toInt(), 0.4f)
					setDrawCircles(false)
					setDrawValues(false)
					setDrawCircleHole(false)
					setDrawHighlightIndicators(true)
					cubicIntensity = 1f
					lineWidth = 2.0f
					isHighlightEnabled = false
				}
			}

			input_ph.data = LineData(sets)
			input_ph.notifyDataSetChanged()
			input_ph.invalidate()
		}

		fun displayStats()
		{
			ph_stats_container.removeAllViews()

			selectedModes.forEach { mode ->
				val stats = arrayListOf<template>()
				stats += header(getString(mode))

				val stat = when (mode)
				{
					INPUT_PH -> phStats
					RUNOFF_PH -> runoffStats
					else -> null
				}

				stat ?: return@forEach
				stat.min?.let {
					stats += data(
						label = getString(R.string.min),
						data = it.formatWhole()
					)
				}

				stat.max?.let {
					stats += data(
						label = getString(R.string.max),
						data = it.formatWhole()
					)
				}

				stat.average?.let {
					stats += data(
						label = getString(R.string.ave),
						data = it.formatWhole()
					)
				}

				if (stats.size > 1) renderStats(ph_stats_container, stats)
			}
		}

		arrayListOf(INPUT_PH, RUNOFF_PH, AVERAGE_PH).forEach { mode ->
			val chip = LayoutInflater.from(context!!).inflate(R.layout.filter_chip_stub, additive_chips_container, false) as Chip
			chip.setText(mode)
			chip.isChecked = true
			chip.setOnCheckedChangeListener { buttonView, isChecked ->
				if (isChecked) selectedModes += mode
				else selectedModes -= mode

				refreshCharts()
				displayStats()
			}

			selectedModes += mode
			ph_chips_container += chip
		}

		with (input_ph) {
			setVisibleYRangeMaximum(max(phStats.max?.toFloat() ?: 0.0f, runoffStats.max?.toFloat() ?: 0.0f), YAxis.AxisDependency.LEFT)
			setDrawGridBackground(false)
			description = null
			isScaleYEnabled = false
			setDrawBorders(false)

			axisLeft.labelCount = 8
			axisLeft.setDrawTopYLabelEntry(true)
			axisLeft.setDrawZeroLine(true)
			axisLeft.setDrawGridLines(false)
			axisLeft.textColor = R.attr.colorOnSurface.resolveColor(context!!)

			axisRight.setDrawLabels(false)
			axisRight.setDrawGridLines(false)
			axisRight.setDrawAxisLine(false)

			xAxis.setDrawGridLines(false)
			xAxis.granularity = 1f
			xAxis.position = XAxis.XAxisPosition.BOTTOM
			xAxis.yOffset = 10f
			xAxis.textColor = R.attr.colorOnSurface.resolveColor(context!!)
			xAxis.valueFormatter = object : ValueFormatter()
			{
				override fun getAxisLabel(value: Float, axis: AxisBase?): String
				{
					return waterDates.getOrNull(value.toInt())?.transform {
						"${total}/${day}${getString(stage.printString).toLowerCase()[0]}"
					} ?: ""
				}
			}

			legend.form = Legend.LegendForm.CIRCLE
			legend.textColor = R.attr.colorOnSurface.resolveColor(context!!)
			legend.isWordWrapEnabled = true
		}

		refreshCharts()
		displayStats()
	}

	private fun renderStats(container: ViewGroup, templates: ArrayList<template>)
	{
		templates.forEach { template ->
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
			container += dataView
		}
	}

	fun styleDataset(context: Context, data: LineDataSet, colour: Int)
	{
		val context = ContextThemeWrapper(context, R.style.AppTheme)
		data.valueTextColor = R.attr.colorAccent.resolveColor(context)
		data.setCircleColor(R.attr.colorAccent.resolveColor(context))
		data.cubicIntensity = 0.2f
		data.lineWidth = 3.0f
		data.setDrawCircleHole(true)
		data.color = colour
		data.setCircleColor(colour)
		data.circleRadius = 4.0f
		data.setDrawHighlightIndicators(true)
		data.isHighlightEnabled = true
		data.highlightLineWidth = 2f
		data.highLightColor = ColorUtils.setAlphaComponent(colour, 96)
		data.setDrawValues(false)
		data.valueFormatter = formatter
	}
}
