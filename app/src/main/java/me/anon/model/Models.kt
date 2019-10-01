package me.anon.model

import android.content.Context
import android.os.Parcelable
import android.preference.PreferenceManager
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import me.anon.grow.R
import me.anon.lib.DateRenderer
import me.anon.lib.TdsUnit
import me.anon.lib.TempUnit
import me.anon.lib.Unit
import me.anon.lib.helper.TimeHelper
import java.util.*

/**
 * Schedule root object holding list of feeding schedules
 */
@Parcelize
@JsonClass(generateAdapter = true)
class FeedingSchedule(
	var id: String = UUID.randomUUID().toString(),
	var name: String = "",
	var description: String = "",
	@field:Json(name = "schedules") var _schedules: ArrayList<FeedingScheduleDate>
) : Parcelable {
	@field:Transient var schedules = _schedules
		get() {
			field.sortWith(compareBy<FeedingScheduleDate> { it.stageRange[0].ordinal }.thenBy { it.dateRange[0] })
			return field
		}

	constructor() : this(
		id = UUID.randomUUID().toString(),
		name = "",
		description = "",
		_schedules = arrayListOf()
	){}
}

/**
 * Feeding schedule for specific date
 */
@Parcelize
@JsonClass(generateAdapter = true)
class FeedingScheduleDate(
	var id: String = UUID.randomUUID().toString(),
	var dateRange: Array<Int>,
	var stageRange: Array<PlantStage>,
	var additives: ArrayList<Additive> = arrayListOf()
) : Parcelable {
	constructor() : this(
		id = UUID.randomUUID().toString(),
		dateRange = arrayOf(),
		stageRange = arrayOf(),
		additives = arrayListOf()
	){}
}

abstract class Action(
	open var date: Long = System.currentTimeMillis(),
	open var notes: String? = null
) : Parcelable
{
	enum class ActionName private constructor(val printString: Int, val colour: Int)
	{
		FIM(R.string.action_fim, -0x65003380),
		FLUSH(R.string.action_flush, -0x65001f7e),
		FOLIAR_FEED(R.string.action_foliar_feed, -0x65191164),
		LST(R.string.action_lst, -0x65000a63),
		LOLLIPOP(R.string.action_lolipop, -0x65002e80),
		PESTICIDE_APPLICATION(R.string.action_pesticide_application, -0x65106566),
		TOP(R.string.action_topped, -0x6543555c),
		TRANSPLANTED(R.string.action_transplanted, -0x65000073),
		TRIM(R.string.action_trim, -0x6500546f),
		TUCK(R.string.action_tuck, -0x65800046);

		companion object
		{
			@JvmStatic
			public fun names(): IntArray
			{
				val names = IntArray(values().size)
				for (index in names.indices)
				{
					names[index] = values()[index].printString
				}

				return names
			}
		}
	}

	override fun equals(o: Any?): Boolean
	{
		if (o === this) return true
		if (o !is Action) return false
		if (!super.equals(o)) return false
		return this.date == o.date
	}
}

@Parcelize
@JsonClass(generateAdapter = true)
class EmptyAction(
	var action: ActionName? = null,

	override var date: Long = System.currentTimeMillis(),
	override var notes: String? = null
) : Action(date, notes)
{
	public var type: String = "Action"
}

@Parcelize
@JsonClass(generateAdapter = true)
class Garden(
	var name: String = "",
	var plantIds: ArrayList<String> = arrayListOf()
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
class NoteAction(
	override var date: Long = System.currentTimeMillis(),
	override var notes: String? = null
) : Action(date, notes)
{
	public var type: String = "Note"
}

@Parcelize
@JsonClass(generateAdapter = true)
class StageChange(
	var newStage: PlantStage = PlantStage.PLANTED,

	override var date: Long = System.currentTimeMillis(),
	override var notes: String? = null
) : Action(date, notes)
{
	public var type: String = "StageChange"
}

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

	/**
	 * Generates summary of the plant.
	 * index 0: planted string
	 * index 1: watered ago
	 * index 2: ph -> runoff amount @ppm temp
	 * index 3: additives
	 * index 4: last note
	 * @param verbosity How verbose the summary is. bigger = more
	 * @return Array list of string lines
	 */
	public fun generateSummary(context: Context, verbosity: Int = 0): ArrayList<String>
	{
		val measureUnit = Unit.getSelectedMeasurementUnit(context)
		val deliveryUnit = Unit.getSelectedDeliveryUnit(context)
		val tempUnit = TempUnit.getSelectedTemperatureUnit(context)
		val usingEc = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("tds_ec", false)

		val summary = arrayListOf<String>()
		var lastWater: Water? = null
		var lastNote: NoteAction? = null
		val stageTimes = calculateStageTime()

		actions?.let { actions ->
			for (index in actions.indices.reversed())
			{
				val action = actions[index]

				if (action.javaClass == Water::class.java && lastWater == null)
				{
					lastWater = action as Water
				}

				if (action.javaClass == NoteAction::class.java && lastNote == null)
				{
					lastNote = action as NoteAction
				}
			}
		}

		var currentStageTime: String? = null
		if (stageTimes.containsKey(stage))
		{
			currentStageTime = when (verbosity)
			{
				0, 1 -> TimeHelper.toDays(stageTimes[stage] ?: 0).toInt().toString() + context.getString(stage.printString).substring(0, 1).toLowerCase()
				else -> "<b>${TimeHelper.toDays(stageTimes[stage] ?: 0).toInt()} ${context.getString(R.string.length_days_inv, context.getString(stage.printString))}</b>"
			}
		}

		if (stage == PlantStage.HARVESTED)
		{
			val stageDate = stageTimes[stage] ?: 0
			val harvested = DateRenderer(context).timeAgo((System.currentTimeMillis() - stageDate).toDouble(), -1)
			val harvestedDays = TimeHelper.toDays(stageDate).toInt()

			summary.add(context.getString(R.string.harvested_ago, "${harvested.time.toInt()} ${context.resources.getQuantityString(harvested.unit.pluralRes, harvested.time.toInt())}") +
				when (verbosity)
				{
					0, 1 -> ""
					else -> " (${context.getString(R.string.length_days, "" + harvestedDays)})"
				}
			)

			if (stageTimes.containsKey(PlantStage.CURING))
			{
				val cureTime = TimeHelper.toDays(stageTimes[PlantStage.CURING] ?: 0).toInt()
				summary.add(context.getString(R.string.length_cured, "" + cureTime))
			}
		}
		else
		{
			val planted = DateRenderer(context).timeAgo(plantDate.toDouble(), -1)
			val plantedDays = DateRenderer(context).timeAgo(plantDate.toDouble(), 3)
			summary.add(when (verbosity)
			{
				0, 1 -> "${plantedDays.time.toInt()}" + currentStageTime?.let {"/$it"}
				else -> context.getString(R.string.planted_ago, "${planted.time.toInt()} ${context.resources.getQuantityString(planted.unit.pluralRes, planted.time.toInt())}") + currentStageTime?.let {", $it"}
			})

			lastWater?.let {
				summary.add(context.getString(R.string.last_watered_ago, DateRenderer(context).timeAgo(it.date.toDouble()).let { time ->
					when (verbosity)
					{
						0, 1 -> time.formattedDate
						else -> time.longFormattedDate
					}
				}))

				if (verbosity > 0)
				{
					var waterStr: String? = null

					it.ph?.let { ph ->
						waterStr = "<b>$ph pH</b> "

						if (verbosity > 1)
						{
							it.runoff?.let { runoff ->
								waterStr += "➙ <b>$runoff pH</b> "
							}
						}
					}

					it.amount?.let { amount ->
						waterStr += "<b>${Unit.ML.to(deliveryUnit, amount)}${deliveryUnit.label}</b> "
					}

					it.tds?.let { tds ->
						var ppm = tds.amount ?: 0.0
						waterStr += "<b>@"
						waterStr += if (tds.type.decimalPlaces == 0) ppm.toInt() else TdsUnit.toTwoDecimalPlaces(ppm)
						waterStr += tds.type.label
						waterStr += "</b> "
					}

					it.temp?.let { temp ->
						val temp = "º${TempUnit.CELCIUS.to(tempUnit, temp)}${tempUnit.label}"
						waterStr += "<b>$temp</b> "
					}

					waterStr?.let { summary.add(it) }

					if (it.additives.isNotEmpty())
					{
						var total = it.additives.sumByDouble { it.amount ?: 0.0 }
						summary += "+ <b>" + Unit.ML.to(measureUnit, total) + measureUnit.label + "</b> " + context.getString(R.string.additives)
					}
				}
			}
		}

		if (verbosity == 2)
		{
			lastNote?.let {
				if (it.notes?.isNotEmpty() == true)
				{
					summary.add("<hr />")
					summary.add(it.notes ?: "")
				}
			}
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
						endDate = System.currentTimeMillis()
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

enum class PlantMedium private constructor(val printString: Int)
{
	SOIL(R.string.soil),
	HYDRO(R.string.hydroponics),
	COCO(R.string.coco_coir),
	AERO(R.string.aeroponics);

	companion object
	{
		fun names(context: Context): Array<String>
		{
			val names = arrayListOf<String>()
			values().forEach { medium ->
				names.add(context.getString(medium.printString))
			}

			return names.toTypedArray()
		}
	}
}

@Parcelize
enum class PlantStage private constructor(val printString: Int) : Parcelable
{
	PLANTED(R.string.planted),
	GERMINATION(R.string.germination),
	SEEDLING(R.string.seedling),
	CUTTING(R.string.cutting),
	VEGETATION(R.string.vegetation),
	FLOWER(R.string.flowering),
	DRYING(R.string.drying),
	CURING(R.string.curing),
	HARVESTED(R.string.harvested);

	companion object
	{
		public fun names(context: Context): Array<String>
		{
			val names = arrayListOf<String>()
			values().forEach { stage ->
				names.add(context.getString(stage.printString))
			}

			return names.toTypedArray()
		}
	}
}

@Parcelize
@JsonClass(generateAdapter = true)
class Water(
	var tds: Tds? = null,
	var ph: Double? = null,
	var runoff: Double? = null,
	var amount: Double? = null,
	var temp: Double? = null,
	var additives: ArrayList<Additive> = arrayListOf(),

	override var date: Long = System.currentTimeMillis(),
	override var notes: String? = null
) : Action(date, notes), Parcelable
{
	public var type: String = "Water"

	@Deprecated("")
	public var ppm: Double? = null
	@Deprecated("")
	public var nutrient: Nutrient? = null
	@Deprecated("")
	public var mlpl: Double? = null

	public fun getSummary(context: Context): String
	{
		val measureUnit = Unit.getSelectedMeasurementUnit(context)
		val deliveryUnit = Unit.getSelectedDeliveryUnit(context)
		val tempUnit = TempUnit.getSelectedTemperatureUnit(context)

		var summary = ""
		var waterStr = StringBuilder()

		ph?.let {
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_ph))
			waterStr.append("</b> ")
			waterStr.append(it)
			waterStr.append(", ")
		}

		runoff?.let {
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_out_ph))
			waterStr.append("</b> ")
			waterStr.append(it)
			waterStr.append(", ")
		}

		summary += if (waterStr.toString().isNotEmpty()) waterStr.toString().substring(0, waterStr.length - 2) + "<br/>" else ""

		waterStr = StringBuilder()

		tds?.let {
			var ppm = it.amount ?: 0.0
			waterStr.append("<b>")
			waterStr.append(context.getString(it.type.strRes))
			waterStr.append("</b> ")
			waterStr.append(if (it.type.decimalPlaces == 0) ppm.toInt() else TdsUnit.toTwoDecimalPlaces(ppm))
			waterStr.append(it.type.label)
			waterStr.append(", ")
		}

		amount?.let {
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_amount))
			waterStr.append("</b> ")
			waterStr.append(Unit.ML.to(deliveryUnit, it))
			waterStr.append(deliveryUnit.label)
			waterStr.append(", ")
		}

		temp?.let {
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_temp))
			waterStr.append("</b> ")
			waterStr.append(TempUnit.CELCIUS.to(tempUnit, it))
			waterStr.append("º").append(tempUnit.label).append(", ")
		}

		summary += if (waterStr.toString().isNotEmpty()) waterStr.toString().substring(0, waterStr.length - 2) + "<br/>" else ""

		waterStr = StringBuilder()

		if (additives.size > 0)
		{
			waterStr.append("<b>")
			waterStr.append(context.getString(R.string.plant_summary_additives))
			waterStr.append("</b> ")

			additives.forEach { additive ->
				if (additive.amount == null) return@forEach

				val converted = Unit.ML.to(measureUnit, additive.amount!!)
				val amountStr = if (converted == Math.floor(converted)) converted.toInt().toString() else converted.toString()

				waterStr.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp; → ")
				waterStr.append(additive.description)
				waterStr.append("  -  ")
				waterStr.append(amountStr)
				waterStr.append(measureUnit.label)
				waterStr.append("/")
				waterStr.append(deliveryUnit.label)
			}
		}

		summary += waterStr.toString()

		return summary
	}
}

@Parcelize
@JsonClass(generateAdapter = true)
class Tds(
	var amount: Double? = null,
	var type: TdsUnit = TdsUnit.PPM500
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
class Additive(
	var amount: Double? = null,
	var description: String? = null
) : Parcelable
