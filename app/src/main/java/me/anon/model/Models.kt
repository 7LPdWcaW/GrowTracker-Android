package me.anon.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Schedule root object holding list of feeding schedules
 */
@Parcelize
class FeedingSchedule(
	val id: String = UUID.randomUUID().toString(),
	var name: String = "",
	var description: String = "",
	var _schedules: ArrayList<FeedingScheduleDate>
) : Parcelable {
	var schedules = _schedules
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
class FeedingScheduleDate(
	val id: String = UUID.randomUUID().toString(),
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
