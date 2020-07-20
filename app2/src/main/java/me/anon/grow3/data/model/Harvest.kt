package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

/**
 * See [me.anon.grow3.ui.logs.view.HarvestLogCard]
 */
@JsonClass(generateAdapter = true)
data class Harvest(
	public var amount: Double
) : Log(action = "Harvest")
