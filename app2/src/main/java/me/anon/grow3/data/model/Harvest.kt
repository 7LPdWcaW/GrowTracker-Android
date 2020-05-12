package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

/**
 * // TODO: Add class description
 */
@JsonClass(generateAdapter = true)
class Harvest(
	public var amount: Double
) : Log(action = "Harvest")
