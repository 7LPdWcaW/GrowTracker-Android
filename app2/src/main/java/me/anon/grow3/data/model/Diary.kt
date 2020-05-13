package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.util.asDateTime
import me.anon.grow3.util.uniqueBy
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

@JsonClass(generateAdapter = true)
class Diary(
	public val id: String = UUID.randomUUID().toString(),
	public var name: String,
	public val log: ArrayList<Log> = arrayListOf(),
	public val crops: ArrayList<Crop> = arrayListOf()
)
{
	private fun cropFilter(crop: Crop?, log: Log)
		= if (crop == null) true else (log.cropIds.isNotEmpty() && log.cropIds.contains(crop.id)) || log.cropIds.isEmpty()

	public fun stage(): Stage = findStage()
	public fun water(): Water? = findWater()
	public fun medium(): Medium? = findMedium()
	public fun environment(): EnvironmentType? = findEnvironmentType()
	public fun size(): Size? = findSize()
	public fun light(): Light? = findLight()
	public fun crop(id: String): Crop = crops.first { it.id == id }

	public fun logOf(id: String): Log? = log.first { it.id == id }

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
					cropIds.add(crop.id)
					date = harvest.date
				}
			}
			else
			{
				this.filterIsInstance<StageChange>()
					.findLast {
						cropFilter(crop, it)
					}
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
						daysInStage += ChronoUnit.DAYS.between(lastStage!!.date.asDateTime(), stageChange.date.asDateTime())
						inStage = false
						lastStage = null
					}
				}

				// ongoing
				if (lastStage == stage) daysInStage += ChronoUnit.DAYS.between(lastStage!!.date.asDateTime(), ZonedDateTime.now())
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
			log += StageChange(StageType.Planted)
		}
	}

	override fun equals(other: Any?): Boolean = (other as? Diary)?.id == id || super.equals(other)
}

public fun Diary(block: Diary.() -> Unit): Diary = Diary(name = "").apply(block)
