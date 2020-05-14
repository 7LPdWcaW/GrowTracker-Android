package me.anon.lib.export

import kotlinx.android.synthetic.main.statistics2_view.*
import me.anon.grow.R
import me.anon.grow.fragment.StatisticsFragment2
import me.anon.lib.TdsUnit
import me.anon.lib.TempUnit
import me.anon.lib.Unit
import me.anon.lib.ext.formatWhole
import me.anon.lib.ext.normalise
import me.anon.lib.helper.MoshiHelper
import me.anon.lib.helper.StatsHelper
import me.anon.lib.helper.TimeHelper
import me.anon.model.*
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.format.DateTimeFormatter
import java.io.ByteArrayInputStream
import java.sql.Timestamp
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.ceil

/**
 * // TODO: Add class description
 */
class MarkdownProcessor : ExportProcessor()
{
	private val NEW_LINE = "\r\n"

	private var documentName = "growlog.md"
	private val documentBuilder = StringBuilder()

	override fun beginDocument(isPlant: Boolean)
	{
		if (!isPlant)
		{
			documentName = "garden.md"
			documentBuilder.append("# Garden")
		}
		else
		{
			documentBuilder.append("# Grow Log")
		}

		documentBuilder.append(NEW_LINE + NEW_LINE)
	}

	override fun endDocument(zipFile: ZipFile, zipPathPrefix: String)
	{
		documentBuilder.append("Generated using [Grow Tracker](https://github.com/7LPdWcaW/GrowTracker-Android)")

		// Write the log
		try
		{
			val parameters = ZipParameters()
			parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
			parameters.fileNameInZip = zipPathPrefix + documentName
			parameters.isSourceExternalStream = true

			zipFile.addStream(ByteArrayInputStream(documentBuilder.toString().toByteArray(charset("UTF-8"))), parameters)
		}
		catch (e: Exception)
		{
			e.printStackTrace()
		}
	}

	override fun printPlantDetails(plant: Plant)
	{
		documentBuilder.append("## Grow details")
		documentBuilder.append(NEW_LINE + NEW_LINE)

		documentBuilder.append("**Name:** ").append(plant.name)
		documentBuilder.append(NEW_LINE + NEW_LINE)

		plant.strain?.let {
			documentBuilder.append("**Strain:** ").append(it)
			documentBuilder.append(NEW_LINE + NEW_LINE)
		}

		val planted = DateTimeUtils.toLocalDateTime(Timestamp(plant.plantDate))
		documentBuilder.append("**Planted:** ").append(planted.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
		documentBuilder.append(NEW_LINE + NEW_LINE)

		documentBuilder.append("**From clone?:** ").append(plant.clone)
		documentBuilder.append(NEW_LINE + NEW_LINE)

		documentBuilder.append("**Medium:** ").append(plant.medium.enString)
		documentBuilder.append(NEW_LINE + NEW_LINE)

		if (plant.mediumDetails?.isNotEmpty() == true)
		{
			documentBuilder.append(plant.mediumDetails)
			documentBuilder.append(NEW_LINE + NEW_LINE)
		}
	}

	override fun printPlantStages(plant: Plant)
	{
		val stageTimes = plant.calculateStageTime()
		val plantStages = plant.getStages()
		val currentStage = plant.stage

		if (currentStage == PlantStage.HARVESTED)
		{
			val stageDate = plant.actions?.find { it is StageChange && it.newStage == PlantStage.HARVESTED }?.date ?: 0
			if (stageDate > 0)
			{
				val harvested = DateTimeUtils.toLocalDateTime(Timestamp(stageDate))

				documentBuilder.append("**Harvested:** ").append(harvested.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
				documentBuilder.append(NEW_LINE + NEW_LINE)
			}
		}

		documentBuilder.append("## Stages")
		documentBuilder.append(NEW_LINE + NEW_LINE)

		plantStages.keys.reversed().forEach { key ->
			val value = plantStages[key]!!
			val stageTime = stageTimes[key]
			val stageDate = value.date
			val stageDateTime = DateTimeUtils.toLocalDateTime(Timestamp(stageDate))
			val notes = value.notes
			val stageName = key.enString

			documentBuilder.append(" - ").append(stageName).append(NEW_LINE + NEW_LINE)
			documentBuilder.append("\t - ").append("**Set on:** ").append(stageDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(NEW_LINE)

			if (key != PlantStage.HARVESTED)
			{
				stageTime?.let {
					documentBuilder.append("\t - ").append("**In stage for:** ").append(TimeHelper.toDays(stageTime).formatWhole()).append(" days").append(NEW_LINE)
				}
			}

			if (notes?.isNotEmpty() == true)
			{
				documentBuilder.append("\t - ").append(notes).append(NEW_LINE)
			}

			documentBuilder.append(NEW_LINE + NEW_LINE)
		}

		documentBuilder.append("![Stages](stages.jpg)").append(NEW_LINE + NEW_LINE)
	}

	override fun printPlantStats(plant: Plant)
	{
		val viewModel = StatisticsFragment2.StatisticsViewModel(
			selectedTds ?: throw Exception(),
			selectedDelivery ?: throw Exception(),
			selectedMeasurement ?: throw Exception(),
			selectedTemp ?: throw Exception(),
			plant
		)

		fun generateGeneralsStats()
		{
			documentBuilder.append("## General Stats")
				.append(NEW_LINE + NEW_LINE)

			// total time
			documentBuilder.append("**").append(context!!.getString(R.string.total_time_label)).append("** ")
				.append("${viewModel.totalDays.formatWhole()} ${context!!.resources.getQuantityString(R.plurals.time_day, viewModel.totalDays.toInt())}")
				.append(NEW_LINE + NEW_LINE)

			documentBuilder.append("## Water stats")
				.append(NEW_LINE + NEW_LINE)

			// total waters
			documentBuilder.append("**").append(context!!.getString(R.string.total_waters_label)).append("** ")
				.append("${viewModel.totalWater.formatWhole()}")
				.append(NEW_LINE + NEW_LINE)

			// total flushes
			documentBuilder.append("**").append(context!!.getString(R.string.total_flushes_label)).append("** ")
				.append("${viewModel.totalFlush.formatWhole()}")
				.append(NEW_LINE + NEW_LINE)

			// total water amount
			documentBuilder.append("**").append(context!!.getString(R.string.total_water_amount_label)).append("** ")
				.append("${Unit.ML.to(viewModel.selectedDeliveryUnit, viewModel.totalWaterAmount).formatWhole()} ${viewModel.selectedDeliveryUnit.label}")
				.append(NEW_LINE + NEW_LINE)

			// average water amount
			documentBuilder.append("**").append(context!!.getString(R.string.ave_water_amount_label)).append("** ")
				.append("${Unit.ML.to(viewModel.selectedDeliveryUnit, (viewModel.totalWaterAmount / viewModel.totalWater.toDouble())).formatWhole()} ${viewModel.selectedDeliveryUnit.label}")
				.append(NEW_LINE + NEW_LINE)

			// ave time between water
			documentBuilder.append("**").append(context!!.getString(R.string.ave_time_between_water_label)).append("** ")
			documentBuilder.append((TimeHelper.toDays(viewModel.waterDifference) / viewModel.totalWater).let { d ->
				"${d.formatWhole()} ${context!!.resources.getQuantityString(R.plurals.time_day, ceil(d).toInt())}"
			}).append(NEW_LINE + NEW_LINE)

			// ave water time between stages
			viewModel.aveStageWaters
				.toSortedMap(Comparator { first, second -> first.ordinal.compareTo(second.ordinal) })
				.forEach { (stage, dates) ->
					if (dates.isNotEmpty())
					{
						var dateDifference = dates.last() - dates.first()
						documentBuilder.append("**").append(context!!.getString(R.string.ave_time_stage_label, stage.enString)).append("** ")
						documentBuilder.append((TimeHelper.toDays(dateDifference) / dates.size).let { d ->
							"${d.formatWhole()} ${context!!.resources.getQuantityString(R.plurals.time_day, ceil(d).toInt())}"
						}).append(NEW_LINE + NEW_LINE)
					}
				}
		}

		fun generateAdditiveStats()
		{
			documentBuilder.append("## Additive stats").append(NEW_LINE + NEW_LINE)

			// add graph

			documentBuilder.append("![Additives](additives.jpg)").append(NEW_LINE + NEW_LINE)
			documentBuilder.append("![Additives over time](additives-over-time.jpg)").append(NEW_LINE + NEW_LINE)
			documentBuilder.append("![Total Additives](total-additives.jpg)").append(NEW_LINE + NEW_LINE)

			viewModel.additiveStats.forEach { (key, stat) ->
				documentBuilder.append("### ").append(context!!.getString(R.string.additive_stat_header, key))
					.append(NEW_LINE + NEW_LINE)

				documentBuilder.append("![$key](${key.normalise()}.jpg)").append(NEW_LINE + NEW_LINE)

				documentBuilder.append("**").append(context!!.getString(R.string.min)).append("** ")
					.append("${Unit.ML.to(viewModel.selectedMeasurementUnit, stat.min).formatWhole()} ${viewModel.selectedMeasurementUnit.label}/${viewModel.selectedDeliveryUnit.label}")
					.append(NEW_LINE + NEW_LINE)
				documentBuilder.append("**").append(context!!.getString(R.string.max)).append("** ")
					.append("${Unit.ML.to(viewModel.selectedMeasurementUnit, stat.max).formatWhole()} ${viewModel.selectedMeasurementUnit.label}/${viewModel.selectedDeliveryUnit.label}")
					.append(NEW_LINE + NEW_LINE)
				documentBuilder.append("**").append(context!!.getString(R.string.additive_average_usage_label)).append("** ")
					.append("${Unit.ML.to(viewModel.selectedMeasurementUnit, stat.total / stat.count.toDouble()).formatWhole()} ${viewModel.selectedMeasurementUnit.label}/${viewModel.selectedDeliveryUnit.label}")
					.append(NEW_LINE + NEW_LINE)
				documentBuilder.append("**").append(context!!.getString(R.string.additive_usage_count_label)).append("** ")
					.append("${stat.count}")
					.append(NEW_LINE + NEW_LINE)
				documentBuilder.append("**").append(context!!.getString(R.string.additive_total_usage_label)).append("** ")
					.append("${Unit.ML.to(viewModel.selectedMeasurementUnit, stat.totalAdjusted).formatWhole()} ${viewModel.selectedMeasurementUnit.label}")
					.append(NEW_LINE + NEW_LINE)
			}
		}

		fun generatePhStats()
		{
			arrayListOf(viewModel.phStats, viewModel.runoffStats).forEach { stat ->
				documentBuilder.append("## ").append(context!!.getString(when (stat)
				{
					viewModel.phStats -> R.string.stat_input_ph
					viewModel.runoffStats -> R.string.stat_runoff_ph
					else -> throw Exception()
				}))
				.append(NEW_LINE + NEW_LINE)

				stat.min?.let {
					documentBuilder.append("**").append(context!!.getString(R.string.min)).append("** ")
					documentBuilder.append(it.formatWhole())
						.append(NEW_LINE + NEW_LINE)
				}

				stat.max?.let {
					documentBuilder.append("**").append(context!!.getString(R.string.max)).append("** ")
					documentBuilder.append(it.formatWhole())
						.append(NEW_LINE + NEW_LINE)
				}

				stat.average?.let {
					documentBuilder.append("**").append(context!!.getString(R.string.ave)).append("** ")
					documentBuilder.append(it.formatWhole())
						.append(NEW_LINE + NEW_LINE)
				}

				val name = when (stat)
				{
					viewModel.phStats -> "input-ph"
					viewModel.runoffStats -> "runoff-ph"
					else -> throw Exception()
				}

				documentBuilder.append("![$name]($name.jpg)").append(NEW_LINE + NEW_LINE)
			}
		}

		fun generateTdsStats()
		{
			viewModel.tdsStats.forEach { (key, stat) ->
				if (stat.average?.isNaN() == false)
				{
					documentBuilder.append("## ").append(context!!.getString(key.strRes))
						.append(NEW_LINE + NEW_LINE)

					stat.min?.let {
						documentBuilder.append("**").append(context!!.getString(R.string.min)).append("** ")
							.append(it.formatWhole())
							.append(NEW_LINE + NEW_LINE)
					}

					stat.max?.let {
						documentBuilder.append("**").append(context!!.getString(R.string.max)).append("** ")
							.append(it.formatWhole())
							.append(NEW_LINE + NEW_LINE)
					}

					stat.average?.let {
						documentBuilder.append("**").append(context!!.getString(R.string.ave)).append("** ")
							.append(it.formatWhole())
							.append(NEW_LINE + NEW_LINE)
					}

					documentBuilder.append("![${key.enStr}](${key.enStr}.jpg)").append(NEW_LINE + NEW_LINE)
				}
			}
		}

		fun generateTempStats()
		{
			documentBuilder.append("## ").append(context!!.getString(R.string.temperature_title))
						.append(NEW_LINE + NEW_LINE)

			viewModel.tempStats.min?.let {
				documentBuilder.append("**").append(context!!.getString(R.string.min)).append("** ")
					.append("${it.formatWhole()}°${viewModel.selectedTempUnit.label}")
					.append(NEW_LINE + NEW_LINE)
			}

			viewModel.tempStats.max?.let {
				documentBuilder.append("**").append(context!!.getString(R.string.max)).append("** ")
					.append("${it.formatWhole()}°${viewModel.selectedTempUnit.label}")
					.append(NEW_LINE + NEW_LINE)
			}

			viewModel.tempStats.average?.let {
				documentBuilder.append("**").append(context!!.getString(R.string.ave)).append("** ")
					.append("${it.formatWhole()}°${viewModel.selectedTempUnit.label}")
					.append(NEW_LINE + NEW_LINE)
			}

			documentBuilder.append("![temp](temp.jpg)").append(NEW_LINE + NEW_LINE)
		}

		generateGeneralsStats()
		generateAdditiveStats()
		generatePhStats()
		generateTdsStats()
		generateTempStats()
	}

	override fun printPlantActions(plant: Plant)
	{
		if (plant.actions.isNullOrEmpty()) return

		documentBuilder.append("## Actions").append(NEW_LINE + NEW_LINE)
		documentBuilder.append("| Date | Stage | Action | Details | Notes |").append(NEW_LINE)
		documentBuilder.append("| ---- | ----- | ------ | ------- | ----- |").append(NEW_LINE)

		plant.actions?.asReversed()?.forEach { action ->
			val actionDate = DateTimeUtils.toLocalDateTime(Timestamp(action.date))

			documentBuilder.append("| ")
			documentBuilder.append(actionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
			documentBuilder.append(" | ")

			// stage date
			var lastChange: StageChange? = null
			val currentChangeDate = action.date

			for (index in plant.actions!!.size - 1 downTo 0)
			{
				val action = plant.actions!![index]
				if (action is StageChange)
				{
					if (action.date < currentChangeDate && lastChange == null)
					{
						lastChange = action
						break
					}
				}
			}

			lastChange?.let {
				val totalDays = TimeHelper.toDays(Math.abs(currentChangeDate - plant.plantDate)).toInt()
				documentBuilder.append(if (totalDays == 0) 1 else totalDays)

				var currentDays = TimeHelper.toDays(Math.abs(currentChangeDate - it.date)).toInt()
				currentDays = if (currentDays == 0) 1 else currentDays
				documentBuilder.append("/" + currentDays + it.newStage.enString.substring(0, 1).toLowerCase())
			} ?: let {
				documentBuilder.append(" ")
			}

			documentBuilder.append(" | ")
			documentBuilder.append(action.getTypeStr())
			documentBuilder.append(" | ")

			when (action)
			{
				is Water -> {
					action.ph?.let { documentBuilder.append("**pH:** ").append(it.formatWhole()).append(" ") }
					action.runoff?.let { documentBuilder.append("**Runoff:** ").append(it.formatWhole()).append(" ") }
					action.amount?.let { documentBuilder.append("**amount:** ").append(Unit.ML.to(selectedDelivery, it).formatWhole()).append(selectedDelivery!!.label).append(" ") }
					action.tds?.let { documentBuilder.append("**${it.type.enStr}:** ").append(it.amount.formatWhole()).append(it.type.label).append(" ") }

					if (action.additives.isNotEmpty())
					{
						documentBuilder.append(" / ")
						val additives = action.additives.sortedBy { it.description ?: "" }
						additives.forEach { additive ->
							documentBuilder.append("**${additive.description ?: ""}:** ").append(Unit.ML.to(selectedMeasurement, additive.amount ?: 0.0).formatWhole()).append(selectedMeasurement!!.label).append("/").append(selectedDelivery!!.label)
							if (additives.last() != additive) documentBuilder.append(" – ")
						}
					}
				}

				is StageChange -> {
					documentBuilder.append("Changed to ${action.newStage.enString}")
				}

				is EmptyAction -> {
					documentBuilder.append(action.action?.enString)
				}

				else -> documentBuilder.append(" ")
			}

			documentBuilder.append(" | ")
			documentBuilder.append((action.notes ?: "").replace("\r", "").replace("\n", ""))
			documentBuilder.append(" |").append(NEW_LINE)
		}

		documentBuilder.append(NEW_LINE)
	}

	override fun printPlantImages(map: SortedMap<String, ArrayList<String>>)
	{
		documentBuilder.append("## Images")
		documentBuilder.append(NEW_LINE + NEW_LINE)

		map.forEach { (k, items) ->
			documentBuilder.append("### $k")
			documentBuilder.append(NEW_LINE + NEW_LINE)
			items.forEach { item ->
				documentBuilder.append("![${item.split("/").last()}]($item)")
				documentBuilder.append(NEW_LINE + NEW_LINE)
			}
		}
	}

	override fun printGardenDetails(garden: Garden)
	{
		documentBuilder.append("## Garden details")
		documentBuilder.append(NEW_LINE + NEW_LINE)

		documentBuilder.append("**Name:** ").append(garden.name)
		documentBuilder.append(NEW_LINE + NEW_LINE)
	}

	override fun printGardenStats(garden: Garden)
	{
		val aveTemp = arrayOfNulls<String>(3)
		StatsHelper.setTempData(garden, null, selectedTemp, null, aveTemp)
		documentBuilder.append("### Temperature (°${selectedTemp!!.label})").append(NEW_LINE + NEW_LINE)
		documentBuilder.append(" - **Minimum temperature:** ").append(aveTemp[0]).append("°${selectedTemp!!.label}").append(NEW_LINE)
		documentBuilder.append(" - **Maximum temperature:** ").append(aveTemp[1]).append("°${selectedTemp!!.label}").append(NEW_LINE)
		documentBuilder.append(" - **Average temperature:** ").append(aveTemp[2]).append("°${selectedTemp!!.label}").append(NEW_LINE + NEW_LINE)
		documentBuilder.append("![Temp](garden-temp.jpg)").append(NEW_LINE + NEW_LINE)

		val aveHumidity = arrayOfNulls<String>(3)
		StatsHelper.setHumidityData(garden, null, null, aveHumidity)
		documentBuilder.append("### Humidity").append(NEW_LINE + NEW_LINE)
		documentBuilder.append(" - **Minimum humidity:** ").append(aveHumidity[0]).append("%").append(NEW_LINE)
		documentBuilder.append(" - **Maximum humidity:** ").append(aveHumidity[1]).append("%").append(NEW_LINE)
		documentBuilder.append(" - **Average humidity:** ").append(aveHumidity[2]).append("%").append(NEW_LINE + NEW_LINE)
		documentBuilder.append("![Humidity](garden-humidity.jpg)").append(NEW_LINE + NEW_LINE)
	}

	override fun printGardenActions(garden: Garden)
	{
		if (garden.actions.isNullOrEmpty()) return

		documentBuilder.append("## Actions").append(NEW_LINE + NEW_LINE)
		documentBuilder.append("| Date | Action | Details | Notes |").append(NEW_LINE)
		documentBuilder.append("| ---- | ------ | ------- | ----- |").append(NEW_LINE)

		garden.actions.asReversed().forEach { action ->
			val actionDate = DateTimeUtils.toLocalDateTime(Timestamp(action.date))

			documentBuilder.append("| ")
			documentBuilder.append(actionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
			documentBuilder.append(" | ")
			documentBuilder.append(action.getTypeStr())
			documentBuilder.append(" | ")

			when (action)
			{
				is TemperatureChange -> {
					documentBuilder.append(TempUnit.CELCIUS.to(selectedTemp, action.temp).formatWhole()).append("°${selectedTemp!!.label}")
				}

				is HumidityChange -> {
					documentBuilder.append(action.humidity.formatWhole()).append("%")
				}

				is LightingChange -> {
					documentBuilder.append("**Lights on:** ").append(action.on).append(" **Lights off::** ").append(action.off)
				}

				else -> documentBuilder.append(" ")
			}

			documentBuilder.append(" | ")
			documentBuilder.append((action.notes ?: "").replace("\r", "").replace("\n", ""))
			documentBuilder.append(" |").append(NEW_LINE)
		}

		documentBuilder.append(NEW_LINE)
		documentBuilder.append("## Raw garden data").append(NEW_LINE).append(NEW_LINE)
		documentBuilder.append("```").append("\r\n").append(MoshiHelper.toJson(garden, Garden::class.java)).append("\r\n").append("```").append(NEW_LINE).append(NEW_LINE)
	}

	override fun printRaw(plant: Plant)
	{
		documentBuilder.append("## Raw plant data").append(NEW_LINE).append(NEW_LINE)
		documentBuilder.append("```").append("\r\n").append(MoshiHelper.toJson(plant, Plant::class.java)).append("\r\n").append("```").append(NEW_LINE).append(NEW_LINE)
	}
}
