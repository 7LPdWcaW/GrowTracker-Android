package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

typealias Stage = StageChange

@JsonClass(generateAdapter = true)
class StageChange(
	public var type: StageType
) : Log(action = "StageChange")
