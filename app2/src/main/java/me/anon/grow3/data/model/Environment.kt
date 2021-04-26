package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

/**
 * See [me.anon.grow3.ui.logs.view.EnvironmentLogCard]
 */
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
	override var cropIds: List<String>
		get() = listOf()
		set(value){}
}

@JsonClass(generateAdapter = true)
data class Size(
	public var width: Dimension? = null,
	public var height: Dimension? = null,
	public var depth: Dimension? = null
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
