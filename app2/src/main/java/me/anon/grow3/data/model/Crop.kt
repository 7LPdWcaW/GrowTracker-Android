package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.util.asString
import org.threeten.bp.ZonedDateTime
import java.util.*

@JsonClass(generateAdapter = true)
data class Crop(
	public val id: String = UUID.randomUUID().toString(),
	public var name: String,
	public var genetics: String?,
	public var numberOfPlants: Int = 1,
	//public var cloneOf: String? = null,
	public var platedDate: String = ZonedDateTime.now().asString()
)
