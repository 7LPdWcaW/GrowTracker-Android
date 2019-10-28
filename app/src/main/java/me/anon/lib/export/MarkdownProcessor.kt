package me.anon.lib.export

import me.anon.lib.TdsUnit
import me.anon.lib.TempUnit
import me.anon.lib.Unit
import me.anon.lib.ext.formatWhole
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
	}

	override fun printPlantStats(plant: Plant)
	{
		val startDate = plant.plantDate
		var endDate = System.currentTimeMillis()
		var waterDifference = 0L
		var lastWater = 0L
		var totalWater = 0
		var totalWaterUsed = 0.0
		var totalFlush = 0
		val additives = HashMap<String, Double>()
		val tdsNames = TreeSet<TdsUnit>()

		plant.actions?.forEach { action ->
			if (action is StageChange)
			{
				if (action.newStage === PlantStage.HARVESTED)
				{
					endDate = action.date
				}
			}

			if (action is Water)
			{
				if (lastWater != 0L)
				{
					waterDifference += Math.abs(action.date - lastWater)
				}

				totalWaterUsed += action.amount ?: 0.0

				val actionAdditives = action.additives
				for (additive in actionAdditives)
				{
					val delivery: Double = action.amount ?: 1.0

					if (!additives.containsKey(additive.description))
					{
						additives.put(additive.description ?: "", 0.0)
					}

					val totalDelivery = Unit.ML.to(selectedDelivery, delivery)
					val additiveAmount = Unit.ML.to(selectedMeasurement, additive.amount!!)

					additives.put(additive.description ?: "", additives[additive.description]!! + (additiveAmount * totalDelivery))
				}

				if (action.tds != null)
				{
					tdsNames.add(action.tds!!.type)
				}

				totalWater++
				lastWater = action.date
			}

			if (action is EmptyAction && action.action === Action.ActionName.FLUSH)
			{
				totalFlush++
			}
		}

		val seconds = (endDate - startDate) / 1000
		val days = seconds.toDouble() * 0.0000115741

		documentBuilder.append("## Stats").append(NEW_LINE + NEW_LINE)
		documentBuilder.append(" - **Total grow time:** ").append(String.format("%1$,.2f days", days)).append(NEW_LINE)
		documentBuilder.append(" - **Total waters:** ").append(totalWater.toString()).append(NEW_LINE)
		documentBuilder.append(" - **Total water used:** ").append(Unit.ML.to(selectedDelivery, totalWaterUsed).formatWhole()).append(selectedDelivery!!.label).append(NEW_LINE)
		documentBuilder.append(" - **Total flushes:** ").append(totalFlush.toString()).append(NEW_LINE)
		documentBuilder.append(" - **Average time between watering:** ").append(String.format("%1$,.2f days", TimeHelper.toDays(waterDifference) / totalWater.toDouble())).append(NEW_LINE + NEW_LINE)

		if (additives.isNotEmpty())
		{
			documentBuilder.append("### Nutrients used").append(NEW_LINE + NEW_LINE)
			additives.forEach { (k, v) ->
				documentBuilder.append(" - ").append(k)
					.append(" (total: ").append(Unit.ML.to(selectedMeasurement!!, v).formatWhole()).append(selectedMeasurement!!.label).append(")")
					.append(NEW_LINE)
			}
			documentBuilder.append(NEW_LINE)
		}

		documentBuilder.append("![Additives](additives.jpg)").append(NEW_LINE + NEW_LINE)

		val avePh = arrayOfNulls<String>(3)
		StatsHelper.setInputData(plant, null, null, avePh)
		documentBuilder.append("### Input/runoff pH").append(NEW_LINE + NEW_LINE)
		documentBuilder.append(" - **Minimum input pH:** ").append(avePh[0]).append(NEW_LINE)
		documentBuilder.append(" - **Maximum input pH:** ").append(avePh[1]).append(NEW_LINE)
		documentBuilder.append(" - **Average input pH:** ").append(avePh[2]).append(NEW_LINE + NEW_LINE)
		documentBuilder.append("![pH Chart](input-ph.jpg)").append(NEW_LINE + NEW_LINE)

		tdsNames.forEach { tds ->
			documentBuilder.append("### ${tds.enStr}").append(NEW_LINE + NEW_LINE)

			val tdsArr = arrayOfNulls<String>(3)
			StatsHelper.setTdsData(plant, null, null, tdsArr, tds)
			documentBuilder.append(" - **Minimum input ${tds.enStr}**: ").append(tdsArr[0]).append(tds.label).append(NEW_LINE)
			documentBuilder.append(" - **Maximum input ${tds.enStr}**: ").append(tdsArr[1]).append(tds.label).append(NEW_LINE)
			documentBuilder.append(" - **Average input ${tds.enStr}**: ").append(tdsArr[2]).append(tds.label).append(NEW_LINE + NEW_LINE)
			documentBuilder.append("![${tds.enStr} Chart](${tds.enStr}.jpg)").append(NEW_LINE + NEW_LINE)
		}

		val aveTemp = arrayOfNulls<String>(3)
		StatsHelper.setTempData(plant, null, selectedTemp, null, aveTemp)
		documentBuilder.append("### Temperature (°${selectedTemp!!.label})").append(NEW_LINE + NEW_LINE)
		documentBuilder.append(" - **Minimum input temperature**: ").append(aveTemp[0]).append("°${selectedTemp!!.label}").append(NEW_LINE)
		documentBuilder.append(" - **Maximum input temperature**: ").append(aveTemp[1]).append("°${selectedTemp!!.label}").append(NEW_LINE)
		documentBuilder.append(" - **Average input temperature**: ").append(aveTemp[2]).append("°${selectedTemp!!.label}").append(NEW_LINE + NEW_LINE)
		documentBuilder.append("![Temp](temp.jpg)").append(NEW_LINE + NEW_LINE)
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
						action.additives.forEach { additive ->
							documentBuilder.append("**${additive.description ?: ""}:** ").append(Unit.ML.to(selectedMeasurement, additive.amount ?: 0.0).formatWhole()).append(selectedMeasurement!!.label).append("/").append(selectedDelivery!!.label)
							if (action.additives.last() != additive) documentBuilder.append(" – ")
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

	override fun printPlantImages(arrayList: ArrayList<String>)
	{
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
		documentBuilder.append(" - **Minimum temperature**: ").append(aveTemp[0]).append("°${selectedTemp!!.label}").append(NEW_LINE)
		documentBuilder.append(" - **Maximum temperature**: ").append(aveTemp[1]).append("°${selectedTemp!!.label}").append(NEW_LINE)
		documentBuilder.append(" - **Average temperature**: ").append(aveTemp[2]).append("°${selectedTemp!!.label}").append(NEW_LINE + NEW_LINE)
		documentBuilder.append("![Temp](garden-temp.jpg)").append(NEW_LINE + NEW_LINE)

		val aveHumidity = arrayOfNulls<String>(3)
		StatsHelper.setHumidityData(garden, null, null, aveHumidity)
		documentBuilder.append("### Humidity").append(NEW_LINE + NEW_LINE)
		documentBuilder.append(" - **Minimum humidity**: ").append(aveHumidity[0]).append("%").append(NEW_LINE)
		documentBuilder.append(" - **Maximum humidity**: ").append(aveHumidity[1]).append("%").append(NEW_LINE)
		documentBuilder.append(" - **Average humidity**: ").append(aveHumidity[2]).append("%").append(NEW_LINE + NEW_LINE)
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
					documentBuilder.append("**Lights on:** ").append(action.on).append(" **Lights off:**: ").append(action.off)
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
