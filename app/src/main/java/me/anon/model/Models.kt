package me.anon.model

import android.util.Range
import java.util.*

/**
 * Schedule root object holding list of feeding schedules
 */
class FeedingSchedule(
	val id: String = UUID.randomUUID().toString(),
	var name: String = "",
	var schedules: ArrayList<FeedingScheduleDate> = arrayListOf()
)

/**
 * Feeding schedule for specific date
 */
class FeedingScheduleDate(
	val id: String = UUID.randomUUID().toString(),
	var dateRange: Range<Long>,
	var stageRange: Range<PlantStage>,
	var additives: ArrayList<Additive> = arrayListOf()
)
