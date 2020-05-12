package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

/**
 * // TODO: Add class description
 */
@JsonClass(generateAdapter = true)
class Maintenance(
	public var type: MaintenanceType
) : Log(action = "Maintenance")
