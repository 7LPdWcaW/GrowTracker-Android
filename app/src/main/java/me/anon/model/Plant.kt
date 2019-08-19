package me.anon.model

import android.content.Context
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import me.anon.grow.R
import me.anon.lib.DateRenderer
import me.anon.lib.Unit
import me.anon.lib.helper.TimeHelper
import java.util.*

/**
 * Plant model
 */
@Parcelize
@JsonClass(generateAdapter = true)
class Plant(
	var id: String = UUID.randomUUID().toString(),
	var name: String = "",
	var strain: String? = null,
	var plantDate: Long = System.currentTimeMillis(),
	var clone: Boolean = false,
	var medium: PlantMedium = PlantMedium.SOIL,
	var mediumDetails: String? = null,
	var images: ArrayList<String>? = arrayListOf(),
	var actions: ArrayList<Action>? = arrayListOf()
) : Parcelable
{
	public val stage: PlantStage
		get() {
			actions?.let {
				for (index in it.indices.reversed())
				{
					if (it[index] is StageChange)
					{
						return (it[index] as StageChange).newStage
					}
				}
			}

			// Apparently this could be reached
			return PlantStage.PLANTED
		}

	public fun generateShortSummary(context: Context): String
	{
		val measureUnit = Unit.getSelectedMeasurementUnit(context)
		val deliveryUnit = Unit.getSelectedDeliveryUnit(context)

		var summary = ""

		if (stage == PlantStage.HARVESTED)
		{
			summary += context.getString(R.string.harvested)
		}
		else
		{
			val planted = DateRenderer().timeAgo(plantDate.toDouble(), 3)
			summary += "<b>" + planted.time.toInt() + " " + planted.unit.type + "</b>"

			actions?.let { actions ->
				var lastWater: Water? = null

				val actions = actions
				for (index in actions.indices.reversed())
				{
					val action = actions[index]

					if (action.javaClass == Water::class.java && lastWater == null)
					{
						lastWater = action as Water
					}
				}

				val stageTimes = calculateStageTime()

				if (stageTimes.containsKey(stage))
				{
					summary += " / <b>${TimeHelper.toDays(stageTimes[stage] ?: 0).toInt()}${context.getString(stage!!.printString).substring(0, 1).toLowerCase()}</b>"
				}

				if (lastWater != null)
				{
					summary += "<br/>"
					summary += context.getString(R.string.watered_ago, DateRenderer().timeAgo(lastWater.date.toDouble()).formattedDate)
					summary += "<br/>"

					lastWater.ph?.let { ph ->
						summary += "<b>$ph pH</b> "

						lastWater.runoff?.let { runoff ->
							summary += "➙ <b>$runoff} pH</b> "
						}
					}

					lastWater.amount?.let {
						summary += "<b>${Unit.ML.to(deliveryUnit, lastWater.amount!!)}${deliveryUnit.label}</b>"
					}
				}
			}
		}

		if (summary.endsWith("<br/>"))
		{
			summary = summary.substring(0, summary.length - "<br/>".length)
		}

		return summary
	}

	public fun generateLongSummary(context: Context): String
	{
		val measureUnit = Unit.getSelectedMeasurementUnit(context)
		val deliveryUnit = Unit.getSelectedDeliveryUnit(context)

		var summary = ""

		strain?.let {
			summary += "$it - "
		}

		if (stage == PlantStage.HARVESTED)
		{
			summary += context.getString(R.string.harvested)
		}
		else
		{
			val planted = DateRenderer().timeAgo(plantDate.toDouble(), 3)
			summary += "<b>"
			summary += context.getString(R.string.planted_ago, planted.time.toInt().toString() + " " + planted.unit.type)
			summary += "</b>"

			actions?.let { actions ->
				var lastWater: Water? = null

				val actions = actions
				for (index in actions.indices.reversed())
				{
					val action = actions[index]

					if (action.javaClass == Water::class.java && lastWater == null)
					{
						lastWater = action as Water
					}
				}

				val stageTimes = calculateStageTime()

				if (stageTimes.containsKey(stage))
				{
					summary += " / <b>${TimeHelper.toDays(stageTimes[stage] ?: 0).toInt()}${context.getString(stage.printString).substring(0, 1).toLowerCase()}</b>"
				}

				lastWater?.let {
					summary += "<br/><br/>"
					summary += context.getString(R.string.last_watered_ago, DateRenderer().timeAgo(lastWater.date.toDouble()).formattedDate)
					summary += "<br/>"

					lastWater.ph?.let { ph ->
						summary += "<b>$ph pH</b> "

						lastWater.runoff?.let { runoff ->
							summary += "➙ <b>$runoff pH</b> "
						}
					}

					lastWater.amount?.let {
						summary += "<b>${Unit.ML.to(deliveryUnit, lastWater.amount!!)}${deliveryUnit.label}</b>"
					}

					lastWater.additives?.let {
						var total = it.sumByDouble { it.amount ?: 0.0 }
						summary += "<br/> + <b>" + Unit.ML.to(measureUnit, total) + measureUnit.label + "</b> " + context.getString(R.string.additives)
					}
				}
			}
		}

		if (summary.endsWith("<br/>"))
		{
			summary = summary.substring(0, summary.length - "<br/>".length)
		}

		return summary
	}

	/**
	 * Returns a map of plant stages
	 * @return
	 */
	public fun getStages(): LinkedHashMap<PlantStage, Action>
	{
		val stages = LinkedHashMap<PlantStage, Action>()

		actions?.let { actions ->
			for (index in actions.indices.reversed())
			{
				if (actions[index] is StageChange)
				{
					stages[(actions[index] as StageChange).newStage] = actions[index]
				}
			}

			if (stages.isEmpty())
			{
				val stageChange = StageChange(PlantStage.PLANTED)
				stageChange.date = plantDate
				stages[PlantStage.PLANTED] = stageChange
			}
		}

		return stages
	}

	/**
	 * Calculates the time spent in each plant stage
	 *
	 * @return The list of plant stages with time in milliseconds. Keys are in order of stage defined in [PlantStage]
	 */
	public fun calculateStageTime(): SortedMap<PlantStage, Long>
	{
		val startDate = plantDate
		var endDate = System.currentTimeMillis()
		val stages = TreeMap<PlantStage, Long>(Comparator { lhs, rhs ->
			if (lhs.ordinal < rhs.ordinal)
			{
				return@Comparator 1
			}
			else if (lhs.ordinal > rhs.ordinal)
			{
				return@Comparator -1
			}

			0
		})

		actions?.let { actions ->
			for (action in actions)
			{
				if (action is StageChange)
				{
					stages[action.newStage] = action.date

					if (action.newStage == PlantStage.HARVESTED)
					{
						endDate = action.date
					}
				}
			}
		}

		var stageIndex = 0
		var lastStage: Long = 0
		if (!stages.isEmpty())
		{
			var previous = stages.firstKey()
			for (plantStage in stages.keys)
			{
				var difference: Long = 0
				if (stageIndex == 0)
				{
					difference = endDate - (stages[plantStage] ?: 0)
				}
				else
				{
					difference = lastStage - (stages[plantStage] ?: 0)
				}

				previous = plantStage
				lastStage = stages[plantStage] ?: 0
				stageIndex++

				stages[plantStage] = difference
			}
		}
		else
		{
			val planted = PlantStage.PLANTED
			stages[planted] = 0L
		}

		return stages
	}
}
