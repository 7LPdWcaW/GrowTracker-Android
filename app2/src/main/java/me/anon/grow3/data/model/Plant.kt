package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.util.asString
import org.threeten.bp.ZonedDateTime
import java.util.*

/**
 * // TODO: Add class description
 */
@JsonClass(generateAdapter = true)
class Plant(
	public val id: String = UUID.randomUUID().toString(),
	public var name: String,
	public var genetics: String,
	public var platedDate: String = ZonedDateTime.now().asString()
)
{
//	@Transient
//	public var medium: Medium? = findMedium()
//		set(value) = TODO()
//
//	@Transient
//	public var stageChange: StageChange? = findStage()
//		set(value) = TODO()
}
