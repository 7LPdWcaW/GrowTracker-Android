package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.util.*
import org.dizitart.no2.objects.Id
import org.threeten.bp.ZonedDateTime
import java.util.*

@JsonClass(generateAdapter = true)
data class Diary(
	@Id public val id: String = UUID.randomUUID().toString(),
	public var name: String,
	public var date: String = ZonedDateTime.now().asApiString(),
	public val log: List<Log> = arrayListOf(),
	public val crops: List<Crop> = arrayListOf()
)
{
	public var isDraft = false

	private fun cropFilter(crop: Crop?, log: Log)
		= if (crop == null) true else (log.cropIds.isNotEmpty() && log.cropIds.contains(crop.id)) || log.cropIds.isEmpty()

	public fun stage(): Stage = findStage()
	public fun water(): Water? = findWater()
	public fun medium(): Medium? = findMedium()
	public fun environment(): EnvironmentType? = findEnvironmentType()
	public fun size(): Size? = findSize()
	public fun light(): Light? = findLight()
	public fun crop(id: String): Crop = crops.first { it.id == id }
	public fun stages(): List<Stage> = findAllStages().filter { it.cropIds.isEmpty() }

	/**
	 * Calculates the stage info at the time of the given log for the provided crop
	 */
	public fun stageWhen(log: Log): StageAt
	{
		val stage = stages()
			.last { it.date.asDateTime() <= log.date.asDateTime() } // should at least return Planted

		return StageAt(
			days = (stage.date and log.date).dateDifferenceDays(),
			stage = stage,
			log = log,
			total = (date and log.date).dateDifferenceDays()
		)
	}

	/**
	 * Calculates the stage info at the time of the given log for the provided crop
	 */
	public fun stageWhen(crop: Crop, log: Log): StageAt
	{
		val stage = stagesOf(crop)
			.last { it.date.asDateTime() <= log.date.asDateTime() } // should at least return Planted

		return StageAt(
			days = (stage.date and log.date).dateDifferenceDays(),
			stage = stage,
			log = log,
			total = (crop.platedDate and log.date).dateDifferenceDays()
		)
	}

	/**
	 * Adds or updates a log in the diary
	 */
	public fun log(log: Log): Log
	{
		val index = this.log.indexOfFirst { it.id == log.id }

		if (index > -1) (this.log as ArrayList)[index] = log
		else this.log as ArrayList += log

		this.log.sortBy { it.date }
		return log
	}

	public fun logOf(id: String): Log? = log.firstOrNull { it.id == id }
	public inline fun <reified T> Diary.logOf(id: String): T? = this.logOf(id) as T?

	public fun harvestedOf(id: String): Harvest? = harvestedOf(crop(id))
	public fun harvestedOf(crop: Crop): Harvest?
		= log.sortedBy { it.date }
			.filterIsInstance<Harvest>()
			.lastOrNull()

	public fun stageOf(id: String) = stageOf(crop(id))
	public fun stageOf(crop: Crop): Stage?
		= with (log.sortedBy { it.date }) {
			val harvest = harvestedOf(crop.id)
			if (harvest != null)
			{
				StageChange(StageType.Harvested).apply {
					cropIds as ArrayList += crop.id
					date = harvest.date
				}
			}
			else
			{
				findAllStages(crop)
					.lastOrNull()
			}
		}

	public fun mediumOf(id: String) = mediumOf(crop(id))
	public fun mediumOf(crop: Crop): Medium?
		= log.sortedBy { it.date }
			.filterIsInstance<Transplant>()
			.findLast {
				cropFilter(crop, it)
			}

	public fun stagesOf(id: String) = stagesOf(crop(id))
	public fun stagesOf(crop: Crop): List<Stage> = findAllStages(crop)

	public fun mapCropStages(): Map<Crop, Stage> = crops.associateWith { findStage(it) }

	public fun mapCropStageLengths(): Map<Crop, Map<Stage, Double>>
	{
		return crops.associateWith { crop ->
			val stages = findAllStages(crop)
			val unique = stages.uniqueBy { it.type }
			val ret = unique.associateWith { 0.0 }.toMutableMap()

			// loop through each stage until we move from stage -> different stage
			unique.forEach { stage ->
				var inStage = false
				var daysInStage = 0.0
				var lastStage: Stage? = null
				stages.forEach { stageChange ->
					if (!inStage && stageChange.type == stage.type)
					{
						inStage = true
						lastStage = stageChange
					}
					else if (inStage && lastStage != null)
					{
						daysInStage += (lastStage!!.date and stageChange.date).dateDifferenceDays()
						inStage = false
						lastStage = null
					}
				}

				// ongoing
				if (lastStage == stage) daysInStage += (lastStage!!.date and ZonedDateTime.now().asApiString()).dateDifferenceDays()
				ret[stage] = ret.getOrElse(stage, { 0.0 }) + daysInStage
			}

			ret
		}
	}

	private fun findAllStages(crop: Crop? = null): List<Stage>
		= log.sortedBy { it.date }
			.filterIsInstance<StageChange>()
			.filter {
				cropFilter(crop, it)
			}

	private fun findStage(crop: Crop? = null): Stage
		= log.sortedBy { it.date }
			.filterIsInstance<StageChange>()
			.findLast {
				cropFilter(crop, it)
			}!!

	private fun findEnvironmentType(): EnvironmentType?
		= log.sortedBy { it.date }
			.filterIsInstance<Environment>()
			.findLast { it.type != null }?.type

	private fun findSize(): Size?
		= log.sortedBy { it.date }
			.filterIsInstance<Environment>()
			.findLast { it.size != null }?.size

	private fun findLight(): Light?
		= log.sortedBy { it.date }
			.filterIsInstance<Environment>()
			.findLast { it.light != null }?.light

	private fun findMedium(): Medium?
		= log.sortedBy { it.date }
			.filterIsInstance<Medium>()
			.lastOrNull()

	private fun findWater(): Water?
		= log.sortedBy { it.date }
			.filterIsInstance<Water>()
			.lastOrNull()

	init {
		if (log.isEmpty() || !log.any { it is StageChange })
		{
			log as ArrayList += StageChange(StageType.Planted)
		}

		log.sortedBy { it.date }
	}

	override fun equals(other: Any?): Boolean = (other as? Diary)?.id == id || super.equals(other)
}

public fun Diary(block: Diary.() -> Unit): Diary = Diary(name = "").apply(block)
