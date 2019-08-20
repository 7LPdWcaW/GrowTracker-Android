package me.anon.model

import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

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
