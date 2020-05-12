package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

typealias Type = EnvironmentType

/**
 * // TODO: Add class description
 */
@JsonClass(generateAdapter = true)
class Environment(
	public var type: EnvironmentType?,
	public var temperature: Double?,
	public var humidity: Double?,
	public var relativeHumidity: Double?,
	public var size: Size?,
	public var light: Light?,
	public var schedule: LightSchedule?
) : Log(action = "Environment")

/**
 * // TODO: Add class description
 */
@JsonClass(generateAdapter = true)
data class Size(
	public var width: Double,
	public var height: Double,
	public var depth: Double
)

/**
 * // TODO: Add class description
 */
@JsonClass(generateAdapter = true)
data class Light(
	public var type: LightType,
	public var wattage: Double = Double.MAX_VALUE,
	public var brand: String = "DIY"
)

/**
 * Light schedule. Time is expressed as HH:mm
 */
@JsonClass(generateAdapter = true)
data class LightSchedule(
	public var timeOn: String,
	public var timeOff: String
)
