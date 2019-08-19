package me.anon.model

import kotlinx.android.parcel.Parcelize

@Parcelize
class StageChange(
	var newStage: PlantStage = PlantStage.PLANTED,

	override var date: Long = System.currentTimeMillis(),
	override var notes: String? = null
) : Action(date, notes)
