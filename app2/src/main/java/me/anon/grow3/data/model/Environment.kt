package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.util.ValueHolder

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
		set(_){}
}

public fun Environment.patch(
	type: ValueHolder<EnvironmentType?>? = null,
	temperature: ValueHolder<Double?>? = null,
	humidity: ValueHolder<Double?>? = null,
	relativeHumidity: ValueHolder<Double?>? = null,
	size: ValueHolder<Size?>? = null,
	light: ValueHolder<Light?>? = null,
	schedule: ValueHolder<LightSchedule?>? = null
): Environment
{
	return apply {
		type?.patch { this.type = it }
		temperature?.patch { this.temperature = it }
		humidity?.patch { this.humidity = it }
		relativeHumidity?.patch { this.relativeHumidity = it }
		size?.patch { this.size = it }
		light?.patch { this.light = it }
		schedule?.patch { this.schedule = it }
	}
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
