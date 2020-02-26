package me.anon.grow.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.plusAssign
import androidx.fragment.app.Fragment
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
	}
}
