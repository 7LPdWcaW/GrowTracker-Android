package me.anon.grow3.data.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import me.anon.grow3.util.asString
import org.threeten.bp.ZonedDateTime
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
@JsonSubTypes(
	JsonSubTypes.Type(value = Environment::class, name = "Environment"),
	JsonSubTypes.Type(value = Transplant::class, name = "Transplant"),
	JsonSubTypes.Type(value = Harvest::class, name = "Harvest"),
	JsonSubTypes.Type(value = Maintenance::class, name = "Maintenance"),
	JsonSubTypes.Type(value = Pesticide::class, name = "Pesticide"),
	JsonSubTypes.Type(value = Photo::class, name = "Photo"),
	JsonSubTypes.Type(value = StageChange::class, name = "StageChange"),
	JsonSubTypes.Type(value = Water::class, name = "Water")
)
abstract class Log(
	open val id: String = UUID.randomUUID().toString(),
	open var date: String = ZonedDateTime.now().asString(),
	open var notes: String = "",
	open var cropIds: ArrayList<String> = arrayListOf(),
	open var action: String = "Log"
)
{
	open fun summary(): CharSequence = ""
}
