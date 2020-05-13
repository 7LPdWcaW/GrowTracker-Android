package me.anon.grow3.data.model

import me.anon.grow3.util.asString
import org.threeten.bp.ZonedDateTime
import java.util.*

abstract class Log(
	open val id: String = UUID.randomUUID().toString(),
	open var date: String = ZonedDateTime.now().asString(),
	open var notes: String = "",
	open var cropIds: ArrayList<String> = arrayListOf(),
	open var action: String = "Log"
)
