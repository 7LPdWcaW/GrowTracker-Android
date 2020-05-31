package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class Environment(
	public var type: EnvironmentType? = null,
	public var temperature: Double? = null,
	public var humidity: Double? = null,
	public var relativeHumidity: Double? = null,
	public var size: Size? = null,
	public var light: Light? = null,
	public var schedule: LightSchedule? = null
) : Log(action = "Environment")
{
	override var cropIds: ArrayList<String>
		get() = arrayListOf()
		set(value){}
}

@JsonClass(generateAdapter = true)
data class Size(
	public var width: Double? = null,
	public var height: Double? = null,
	public var depth: Double? = null
)

@JsonClass(generateAdapter = true)
data class Light(
	public var type: LightType,
	public var wattage: Double? = null,
	public var brand: String? = null
)

/**
 * Light schedule. Time is expressed as HH:mm
 */
@JsonClass(generateAdapter = true)
data class LightSchedule(
	public var timeOn: String,
	public var timeOff: String
)
