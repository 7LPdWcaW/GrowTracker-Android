package me.anon.model

import java.util.*

/**
 * Schedule root object holding list of feeding schedules
 */
class FeedingSchedule(
	val id: String = UUID.randomUUID().toString(),
	var name: String = "",
	var description: String = "",
	schedules: ArrayList<FeedingScheduleDate>
) {
	var schedules = schedules
		get() {
			field.sortWith(compareBy<FeedingScheduleDate> { it.stageRange[0].ordinal }.thenBy { it.dateRange[0] })
			return field
		}

	constructor() : this(
		id = UUID.randomUUID().toString(),
		name = "",
		description = "",
		schedules = arrayListOf()
	){}
}

/**
 * Feeding schedule for specific date
 */
class FeedingScheduleDate(
	val id: String = UUID.randomUUID().toString(),
	var dateRange: Array<Int>,
	var stageRange: Array<PlantStage>,
	var additives: ArrayList<Additive> = arrayListOf()
) {
	constructor() : this(
		id = UUID.randomUUID().toString(),
		dateRange = arrayOf(),
		stageRange = arrayOf(),
		additives = arrayListOf()
	){}
}
