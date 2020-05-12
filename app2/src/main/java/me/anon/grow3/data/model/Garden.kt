package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.util.asDateTime
import me.anon.grow3.util.uniqueBy
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

/**
 * // TODO: Add class description
 */
@JsonClass(generateAdapter = true)
class Garden(
	public val id: String = UUID.randomUUID().toString(),
	public var name: String,
	public val log: ArrayList<Log> = arrayListOf(),
	public val plants: ArrayList<Plant> = arrayListOf()
)
{
	public fun stage(): Stage = findStage()
	public fun type(): Type? = findType()
	public fun size(): Size? = findSize()
	public fun light(): Light? = findLight()
	public fun plant(id: String): Plant = plants.first { it.id == id }

	public fun mapPlantStages(): Map<Plant, Stage> = plants.associateWith { findStage(it) }

	private fun plantFilter(plant: Plant?, log: Log)
		= if (plant == null) true else (log.plantIds.isNotEmpty() && log.plantIds.contains(plant.id)) || log.plantIds.isEmpty()

	public fun calculatePlantStageLengths(): Map<Plant, Map<Stage, Double>>
		= plants.associateWith { plant ->
			val stages = findAllStages(plant)
			val unique = stages.uniqueBy { it.type }
			val ret = unique.associateWith { 0.0 }.toMutableMap()

			// loop through each stage until we move from stage -> different sstage
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
					}
				}

				// ongoing
				if (lastStage == stage) daysInStage += ChronoUnit.DAYS.between(lastStage!!.date.asDateTime(), ZonedDateTime.now())
				ret[stage] = ret.getOrElse(stage, { 0.0 }) + daysInStage
			}

			ret
		}

	private fun findAllStages(plant: Plant? = null): List<Stage>
		= log.sortedBy { it.date }
			.filterIsInstance<StageChange>()
			.filter {
				plantFilter(plant, it)
			}

	private fun findStage(plant: Plant? = null): Stage
		= log.sortedBy { it.date }
			.filterIsInstance<StageChange>()
			.findLast {
				plantFilter(plant, it)
			}!!

	private fun findType(): Type?
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

	init {
		if (log.isEmpty() || !log.any { it is StageChange })
		{
			log += StageChange(StageType.Planted)
		}
	}

	override fun equals(other: Any?): Boolean = (other as? Garden)?.id == id || super.equals(other)
}
